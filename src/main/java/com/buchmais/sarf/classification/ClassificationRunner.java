package com.buchmais.sarf.classification;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.configuration.*;
import com.buchmais.sarf.classification.criterion.ClassificationInfoDescriptor;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.RuleDescriptor;
import com.buchmais.sarf.classification.criterion.cohesion.CohesionCriterion;
import com.buchmais.sarf.classification.criterion.cohesion.CohesionCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.dependency.*;
import com.buchmais.sarf.classification.criterion.packagenaming.PackageNamingCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.packagenaming.PackageNamingRepository;
import com.buchmais.sarf.classification.criterion.packagenaming.PackageNamingRuleDescriptor;
import com.buchmais.sarf.classification.criterion.typenaming.TypeNamingCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.typenaming.TypeNamingRepository;
import com.buchmais.sarf.classification.criterion.typenaming.TypeNamingRuleDescriptor;
import com.buchmais.sarf.metamodel.ComponentDependsOn;
import com.buchmais.sarf.metamodel.ComponentDescriptor;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buchmais.sarf.repository.MetricRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
public class ClassificationRunner { // TODO: 18.07.2017 AbstractRunner + BenchmarkRunner

    private ActiveClassificationConfiguration activeClassificationConfiguration;

    private static final Logger LOG = LogManager.getLogger(ClassificationRunner.class);

    private static ClassificationRunner ourInstance = new ClassificationRunner();

    public static ClassificationRunner getInstance() {
        return ourInstance;
    }

    private ClassificationRunner() {
    }

    public void run(URI storeUri, URL configUrl, URL benchmarkUrl, Integer iteration) {
        XOManagerFactory factory = setUpDB(storeUri);
        if (iteration != null) {
            loadIteration(iteration);
        } else if (benchmarkUrl != null) {
            runBenchmark(benchmarkUrl);
        } else {
            startNewIteration(configUrl);
        }
        factory.close();
    }

    private Double runBenchmark(URL benchmarkUrl) {
        this.activeClassificationConfiguration = readConfiguration(benchmarkUrl);
        this.setUpData();
        this.activeClassificationConfiguration.materialize();
        Set<ComponentDescriptor> reference = this.activeClassificationConfiguration.execute();
        CohesionCriterion cohesionCriterion = new CohesionCriterion();
        Set<ComponentDescriptor> comp = cohesionCriterion.classify(2, null, false);
        long[] bIds = comp.stream().mapToLong(c -> SARFRunner.xoManager.getId(c)).toArray();
        Map<Long, List<Long>> aToBTags = new HashMap<>();
        // there are two partitionings, create a mapping from each component in the reference to all components of the
        // competing partition, so that for each type present in the reference component, its component mapping is added
        // algorithm based on http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.115.2944&rep=rep1&type=pdf
        // if you don't understand it, don't change it
        TypeRepository typeRepository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        ComponentRepository componentRepository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
        SARFRunner.xoManager.currentTransaction().begin();
        for (TypeDescriptor t : typeRepository.getAllInternalTypes()) {
            Long tId = SARFRunner.xoManager.getId(t);
            for (ComponentDescriptor a : reference) {
                Long aId = SARFRunner.xoManager.getId(a);
                if (componentRepository.containsType(aId, tId)) {
                    for (ComponentDescriptor b : comp) {
                        Long bId = SARFRunner.xoManager.getId(b);
                        if (componentRepository.containsType(bId, tId)) {
                            aToBTags.merge(
                                    aId,
                                    Lists.newArrayList(bId),
                                    (a1, b1) -> {
                                        a1.addAll(b1);
                                        return a1;
                                    }
                            );
                            break;
                        }
                    }
                    break;
                }
            }
        }
        SARFRunner.xoManager.currentTransaction().commit();
        // center components must now be computed, key is center componenet, values are b components of whose the key is the center component
        Map<Long, Set<Long>> aToBCCs = new HashMap<>();
        for (Long bId : bIds) {
            Long ccB = 0L;
            Long count = 0L;
            for (Map.Entry<Long, List<Long>> aToBTag : aToBTags.entrySet()) {
                Long temp = aToBTag.getValue().stream().filter(l -> Objects.equals(l, bId)).count();
                if (temp > count) {
                    count = temp;
                    ccB = aToBTag.getKey();
                }
            }
            aToBCCs.merge(
                    ccB,
                    Sets.newHashSet(bId),
                    (s1, s2) -> {
                        s1.addAll(s2);
                        return s1;
                    }
            );
        }
        // ambiguities can still be part, remove them
        boolean changed = true; // TODO: 18.07.2017 Do while loop
        Map<Long, Set<Long>> bToACCConsidered = new HashMap<>();
        while (aToBCCs.values().stream().filter(bs -> bs.size() > 1).count() > 0 && changed) {
            changed = false;
            for (Map.Entry<Long, Set<Long>> aToBCC : aToBCCs.entrySet()) {
                if (aToBCC.getValue().size() > 1) {
                    Iterator<Long> i = aToBCC.getValue().iterator();
                    Long tX = i.next();
                    Long tY = i.next();
                    Long x1 = aToBTags.get(aToBCC.getKey()).stream().filter(id -> id.equals(tX)).count();
                    Long y1 = aToBTags.get(aToBCC.getKey()).stream().filter(id -> id.equals(tY)).count();
                    Long x2 = 0L;
                    Long aFId = null;
                    Long y2 = 0L;
                    Long aGId = null;
                    for (Map.Entry<Long, List<Long>> aToBTag : aToBTags.entrySet()) {
                        if (!Objects.equals(aToBTag.getKey(), aToBCC.getKey())) {
                            if (aToBTag.getValue().stream().filter(id -> id.equals(tX)).count() > x2 &&
                                    (bToACCConsidered.get(tX) != null || !bToACCConsidered.get(tX).contains(aToBTag.getKey()))) {
                                x2 = aToBTag.getValue().stream().filter(id -> id.equals(tX)).count();
                                aFId = aToBTag.getKey();
                            }
                            if (aToBTag.getValue().stream().filter(id -> id.equals(tY)).count() > x2 &&
                                    (bToACCConsidered.get(tX) != null || !bToACCConsidered.get(tX).contains(aToBTag.getKey()))) {
                                y2 = aToBTag.getValue().stream().filter(id -> id.equals(tY)).count();
                                aGId = aToBTag.getKey();
                            }
                        }
                    }
                    if (x2 == 0 && y2 == 0) {
                        continue;
                    } else if (x2 == 0 || x1 + y2 > x2 + y1) {
                        aToBCC.getValue().remove(tY);
                        aToBCCs.merge(
                                aGId,
                                Sets.newHashSet(tY),
                                (s1, s2) -> {
                                    s1.addAll(s2);
                                    return s1;
                                }
                        );
                        changed = true;
                        bToACCConsidered.merge(
                                tY,
                                Sets.newHashSet(aToBCC.getKey()),
                                (s1, s2) -> {
                                    s1.addAll(s2);
                                    return s1;
                                }
                        );
                    } else if (y2 == 0 || x1 + y2 <= x2 + y1) {
                        aToBCC.getValue().remove(tX);
                        aToBCCs.merge(
                                aFId,
                                Sets.newHashSet(tX),
                                (s1, s2) -> {
                                    s1.addAll(s2);
                                    return s1;
                                }
                        );
                        changed = true;
                        bToACCConsidered.merge(
                                tX,
                                Sets.newHashSet(aToBCC.getKey()),
                                (s1, s2) -> {
                                    s1.addAll(s2);
                                    return s1;
                                }
                        );
                    }
                }
            }
        }
        // there can still be ambiguities
        Long changeCount = 0L;
        Long nextAToCreate = -1L;
        while (aToBCCs.values().stream().filter(bs -> bs.size() > 1).count() > 0) {
            for (Map.Entry<Long, Set<Long>> aToBCC : aToBCCs.entrySet()) {
                if (aToBCC.getValue().size() > 1) {
                    // split component
                    Long bToKeep = 0L;
                    Long max = 0L;
                    for (Long bId : aToBCC.getValue()) {
                        if (aToBTags.get(aToBCC.getKey()).stream().filter(id -> id.equals(bId)).count() > max) {
                            max = aToBTags.get(aToBCC.getKey()).stream().filter(id -> id.equals(bId)).count();
                            bToKeep = bId;
                        }
                    }
                    Long finalBToKeep = bToKeep;
                    for (Long bId : Sets.newHashSet(aToBTags.get(aToBCC.getKey()))) {
                        if (!Objects.equals(bId, bToKeep)) {
                            aToBTags.put(nextAToCreate, aToBTags.get(aToBCC.getKey()).stream().filter(id -> id.equals(bId)).collect(Collectors.toList()));
                            changeCount += aToBTags.get(nextAToCreate).size();
                            nextAToCreate--;
                        }
                    }
                }
            }
        }
        // free of ambiguities, join

        changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<Long, List<Long>> x : aToBTags.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().size())).collect(Collectors.toList())) {
                for (Map.Entry<Long, List<Long>> y : aToBTags.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().size())).collect(Collectors.toList())) {
                    if (!Objects.equals(x.getKey(), y.getKey()) && aToBCCs.containsKey(x.getKey()) && aToBCCs.get(x.getKey()).size() > 0) {
                        if (!aToBCCs.containsKey(y.getKey()) && !x.getValue().isEmpty() &&y.getValue().contains(x.getValue().get(0))) {
                            // join can be applied
                            x.getValue().addAll(y.getValue());
                            y.getValue().clear();
                            changeCount++;
                            changed = true;
                        } else if (x.getValue().size() > 0 && y.getValue().size() > 0 &&
                                aToBCCs.containsKey(y.getKey()) && aToBCCs.get(y.getKey()).size() > 0) {
                            Long a = (long) x.getValue().size();
                            Long b = (long) y.getValue().size();
                            Long alpha = x.getValue().stream().filter(i -> i.equals(aToBCCs.get(x.getKey()).iterator().next())).count();
                            Long beta = y.getValue().stream().filter(i -> i.equals(aToBCCs.get(x.getKey()).iterator().next())).count();
                            Long gamma = y.getValue().stream().filter(i -> i.equals(aToBCCs.get(y.getKey()).iterator().next())).count();
                            Long delta = x.getValue().stream().filter(i -> i.equals(aToBCCs.get(y.getKey()).iterator().next())).count();
                            if (beta >= delta && beta > gamma + 1) {
                                x.getValue().addAll(y.getValue());
                                y.getValue().clear();
                                changeCount++;
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        BiMap<Long, Long> aToB = HashBiMap.create();
        BiMap<Long, Long> bToA;
        for (Map.Entry<Long, Set<Long>> entry : aToBCCs.entrySet()) {
            aToB.put(entry.getKey(), entry.getValue().iterator().next());
        }
        bToA = aToB.inverse();
        // joins done, now merge
        for (Map.Entry<Long, List<Long>> aToBTag : aToBTags.entrySet()) {
            for (Map.Entry<Long, Set<Long>> aToBCC : aToBCCs.entrySet()) {
                if (aToBTag.getKey().equals(aToBCC.getKey())) {
                    Iterator<Long> iter = aToBTag.getValue().iterator();
                    while (iter.hasNext()) {
                        Long id = iter.next();
                        if (!id.equals(aToBCC.getValue().iterator().next())) {
                            iter.remove();
                            aToBTags.get(bToA.get(id)).add(id);
                            changeCount++;

                        }
                    }
                }
            }
        }
        LOG.info("MoJo - Changes Required: " + changeCount);
        Double quality = (1 - changeCount.doubleValue() / typeRepository.countAllInternalTypes()) * 100;
        LOG.info("MoJo - Quality: " + quality + "%");
        return quality;
    }

    public void startNewIteration() {

    }

    public void startNewIteration(URL configUrl) {

        if (configUrl != null) {
            this.activeClassificationConfiguration = readConfiguration(configUrl);
        }
        if (configUrl == null) {
            this.activeClassificationConfiguration = new ActiveClassificationConfiguration(1);
        }
        this.activeClassificationConfiguration.addClassificationCriterion(new CohesionCriterion());
        this.setUpData();
        this.activeClassificationConfiguration.materialize();
        this.activeClassificationConfiguration.execute();
    }

    private ActiveClassificationConfiguration readConfiguration(URL configUrl) {
        ActiveClassificationConfiguration activeClassificationConfiguration = null;
        LOG.info("Reading XML Configuration");
        try {
            URL schemaUrl = SARFRunner.class.getClassLoader().getResource("schema.xsd");
            JAXBContext jaxbContext = JAXBContext.newInstance(ActiveClassificationConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(schemaUrl);
            jaxbUnmarshaller.setSchema(schema);
            LOG.info("Unmarshalling XML Configuration");
            activeClassificationConfiguration = (ActiveClassificationConfiguration) jaxbUnmarshaller.unmarshal(configUrl);
        } catch (JAXBException | SAXException e) {
            LOG.error(e);
        }
        LOG.info("Unmarshalling XML Configuration Successful");
        return activeClassificationConfiguration;
    }

    public ActiveClassificationConfiguration getCurrentIteration() {
        return null;
    }

    public ClassificationConfiguration loadIteration(Integer iteration) {
        return null;
    }

    private void setUpData() {
        ClassificationConfigurationRepository classificationConfigurationRepository = SARFRunner.xoManager.getRepository(ClassificationConfigurationRepository.class);
        if (this.activeClassificationConfiguration.getIteration() == 1) {
            LOG.info("Resetting Data");
            SARFRunner.xoManager.currentTransaction().begin();
            SARFRunner.xoManager.createQuery(
                    "MATCH (sarf:SARF) DETACH DELETE sarf"
            ).execute();
            SARFRunner.xoManager.createQuery(
                    "MATCH (:Type)-[c:COUPLES]-(:Type) DELETE c"
            ).execute();
            SARFRunner.xoManager.currentTransaction().commit();
            SARFRunner.xoManager.currentTransaction().begin();
            LOG.info("Preparing Data Set");
            SARFRunner.xoManager.getRepository(TypeRepository.class).markAllInternalTypes("de.htw");
            SARFRunner.xoManager.currentTransaction().commit();
            TypeCouplingEnricher.enrich();
        } else if (this.activeClassificationConfiguration.getIteration() <= classificationConfigurationRepository.getCurrentConfiguration().getIteration()) {
            LOG.error("Specified Configuration Iteration must be either 1 or " +
                    classificationConfigurationRepository.getCurrentConfiguration().getIteration() + 1);
            System.exit(1);
        }
    }

    private XOManagerFactory setUpDB(URI storeUri) {
        LOG.info("Setting up Database");
        Properties p = new Properties();
        p.put("neo4j.dbms.allow_format_migration", "true");
        XOUnit xoUnit = XOUnit.builder()
                .properties(p)
                .provider(EmbeddedNeo4jXOProvider.class)
                .type(TypeDescriptor.class)
                .type(TypeDependsOnDescriptor.class)
                .type(TypeRepository.class)
                .type(PackageNamingRuleDescriptor.class)
                .type(PackageNamingCriterionDescriptor.class)
                .type(ComponentRepository.class)
                .type(ComponentDescriptor.class)
                .type(ComponentDependsOn.class)
                .type(ClassificationConfigurationDescriptor.class)
                .type(ClassificationInfoDescriptor.class)
                .type(ClassificationConfigurationRepository.class)
                .type(TypeNamingCriterionDescriptor.class)
                .type(DependencyCriterionDescriptor.class)
                .type(DependencyDescriptor.class)
                .type(MetricRepository.class)
                .type(RuleBasedCriterionDescriptor.class)
                .type(RuleDescriptor.class)
                .type(CohesionCriterionDescriptor.class)
                .type(TypeNamingRuleDescriptor.class)
                .type(TypeNamingRepository.class)
                .type(PackageNamingRepository.class)
                .type(DependencyRepository.class)
                .type(AnnotatedByDescriptor.class)
                .type(ExtendsDescriptor.class)
                .type(ImplementsDescriptor.class)
                .uri(storeUri)
                .build();
        XOManagerFactory factory = XO.createXOManagerFactory(xoUnit);
        SARFRunner.xoManager = factory.createXOManager();
        LOG.info("Setting up Database Successful");
        return factory;
    }
}
