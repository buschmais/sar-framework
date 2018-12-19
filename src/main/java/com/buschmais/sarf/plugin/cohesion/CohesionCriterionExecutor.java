package com.buschmais.sarf.plugin.cohesion;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.framework.configuration.ClassificationConfigurationDescriptor;
import com.buschmais.sarf.framework.configuration.ClassificationConfigurationRepository;
import com.buschmais.sarf.framework.configuration.Decomposition;
import com.buschmais.sarf.framework.configuration.Optimization;
import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.framework.repository.ComponentRepository;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.sarf.plugin.api.criterion.ClassificationCriterionExecutor;
import com.buschmais.sarf.plugin.cohesion.evolution.Partitioner;
import com.buschmais.sarf.plugin.cohesion.evolution.Problem;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOManager;
import com.google.common.collect.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
public final class CohesionCriterionExecutor implements ClassificationCriterionExecutor<CohesionCriterionDescriptor> {

    private static final Logger LOG = LogManager.getLogger(CohesionCriterionExecutor.class);

    private XOManager xOManager;

    @Autowired
    public CohesionCriterionExecutor(XOManager xOManager) {
        this.xOManager = xOManager;
    }

    @Override
    public Set<ComponentDescriptor> execute(CohesionCriterionDescriptor descriptor) {
        LOG.info("Partitioning the System");
        List<Long> typeIds = new ArrayList<>();
        this.xOManager.currentTransaction().begin();
        ClassificationConfigurationRepository classificationConfigurationRepository =
            this.xOManager.getRepository(ClassificationConfigurationRepository.class);
        ClassificationConfigurationDescriptor currentConfiguration = classificationConfigurationRepository.getCurrentConfiguration();
        Integer iteration = currentConfiguration.getIteration();
        boolean similarityBased = currentConfiguration.getOptimization() == Optimization.SIMILARITY;
        boolean hierarchical = currentConfiguration.getDecomposition() == Decomposition.DEEP;
        Integer generations = currentConfiguration.getGenerations();
        Integer populationSize = currentConfiguration.getPopulationSize();
        Map<Long, Set<Long>> components = null; // todo get from database
        TypeRepository typeRepository = this.xOManager.getRepository(TypeRepository.class);
        Query.Result<TypeDescriptor> types = typeRepository.getAllInternalTypes();
        for (TypeDescriptor type : types) {
            typeIds.add(this.xOManager.getId(type));
        }
        types.close();
        long[] ids = typeIds.stream().mapToLong(l -> l).sorted().toArray();
        // create initial partitioning
        Map<Long, Set<Long>> initialPartitioning;
        if (components != null && components.size() > 0) {
            //initialPartitioning = initialPartitioningFromComponents(components, ids);
            initialPartitioning = components;
        } else {
            initialPartitioning = inititialPartitioningFromPackageStructure(ids);
        }
        this.xOManager.currentTransaction().commit();

        int componentLevel = 0;
        do {
            LOG.info("Computing Level " + componentLevel + " Components");
            createProblem(ids, similarityBased);
            this.xOManager.currentTransaction().begin();
            Map<Long, Set<Long>> partitioning = Partitioner.partition(ids, initialPartitioning, generations, populationSize, similarityBased);
            this.xOManager.currentTransaction().commit();
            Set<Long> identifiedGroups = materializeGroups(partitioning, iteration, componentLevel, !hierarchical);
            if (!hierarchical) {
                this.xOManager.currentTransaction().begin();
                Set<ComponentDescriptor> res = new HashSet<>();
                for (Long id : identifiedGroups) {
                    ComponentDescriptor cD = this.xOManager.findById(ComponentDescriptor.class, id);
                    res.add(cD);
                }
                this.xOManager.currentTransaction().commit();
                return res;
            }
            ids = identifiedGroups.stream().mapToLong(l -> l).sorted().toArray();
            this.xOManager.currentTransaction().begin();
            ComponentRepository componentRepository = this.xOManager.getRepository(ComponentRepository.class);
            componentRepository.computeCouplingBetweenComponents(ids);
            componentRepository.computeCouplingBetweenComponentsAndTypes(ids);
            componentRepository.computeCouplingBetweenTypesAndComponents(ids);
            componentRepository.computeSimilarityBetweenComponents(ids);
            componentRepository.computeSimilarityBetweenComponentsAndTypes(ids);
            this.xOManager.currentTransaction().commit();
            initialPartitioning = partitioningFromGroups(identifiedGroups);
            componentLevel++;
        } while (ids.length > 1);
        this.xOManager.currentTransaction().begin();
        ComponentDescriptor result = this.xOManager.findById(ComponentDescriptor.class, ids[0]);
        this.xOManager.currentTransaction().commit();
        LOG.info("Partitioning Finished");
        return Sets.newHashSet(result);
    }

    private Problem createProblem(long[] ids, boolean similarityBased) {
        int maxId = (int) Arrays.stream(ids).max().orElse(0);
        Problem p = Problem.newInstance(maxId + 1, maxId + 1, similarityBased);
        LOG.info("Creating Problem");
        Query<Query.Result.CompositeRowObject> query;
        if (similarityBased) {
            query = this.xOManager.createQuery(
                "MATCH\n" +
                    "  (t)-[s:IS_SIMILAR_TO]->(d) \n" +
                    "WHERE\n" +
                    "  ID(t) IN " + Arrays.toString(ids) + " AND ID(d) IN " + Arrays.toString(ids) + "\n" +
                    "RETURN\n" +
                    "  ID(t) AS t, ID(d) AS d, toFloat(s.similarity) AS r");
        } else {
            query = this.xOManager.createQuery(
                "MATCH\n" +
                    "  (t)-[c:COUPLES]->(d) \n" +
                    "WHERE\n" +
                    "  ID(t) IN " + Arrays.toString(ids) + " AND ID(d) IN " + Arrays.toString(ids) + "\n" +
                    "RETURN\n" +
                    "  ID(t) AS t, ID(d) AS d, toFloat(c.coupling) AS r");
        }
        try (Query.Result<Query.Result.CompositeRowObject> res = query.execute()) {
            res.forEach(r -> p.addRelation(r.get("t", Long.class).intValue(), r.get("d", Long.class).intValue(), r.get("r", Double.class)));
        }
        LOG.info("Creating Problem Successful");
        return p;
    }

    private Set<Long> materializeGroups(Map<Long, Set<Long>> partitioning, int iteration, int level, boolean typeWrapper) {
        this.xOManager.currentTransaction().begin();
        Set<Long> identifiedGroups = new HashSet<>();
        ComponentRepository componentRepository = this.xOManager.getRepository(ComponentRepository.class);
        for (Map.Entry<Long, Set<Long>> component : partitioning.entrySet()) {
            if (component.getValue().size() == 1 && !typeWrapper) {
                identifiedGroups.add(component.getValue().iterator().next());
            } else {
                ComponentDescriptor componentDescriptor = this.xOManager.create(ComponentDescriptor.class);
                componentDescriptor.setShape("Component");
                componentDescriptor.setName("COH" + iteration + "L" + level + "#" + component.getKey());
                for (Long id : component.getValue()) {
                    try {
                        ComponentDescriptor cD = this.xOManager.findById(ComponentDescriptor.class, id);
                        componentDescriptor.getContainedComponents().add(cD);
                    } catch (ClassCastException e) {
                        TypeDescriptor tD = this.xOManager.findById(TypeDescriptor.class, id);
                        componentDescriptor.getContainedTypes().add(tD);
                    }
                }
                identifiedGroups.add(this.xOManager.getId(componentDescriptor));
                Query.Result<TypeDescriptor> typeDescriptors = componentRepository.getContainedTypesRecursively(this.xOManager.getId(componentDescriptor));
                Map<String, Long> wordCount = new HashMap<>();
                for (TypeDescriptor typeDescriptor : typeDescriptors) {
                    String[] words = StringUtils.splitByCharacterTypeCamelCase(typeDescriptor.getName());
                    for (String word : words) {
                        if (!word.equals("$") && !word.matches("\\d+")) {
                            wordCount.merge(
                                word,
                                1L,
                                (w1, w2) -> w1 + 1
                            );
                        }
                    }
                }
                ListMultimap<Long, String> sorted = new ImmutableListMultimap.Builder<Long, String>()
                    .orderKeysBy(Ordering.natural().reverse())
                    .putAll(Multimaps.invertFrom(Multimaps.forMap(wordCount), ArrayListMultimap.create()))
                    .build();
                componentDescriptor.setTopWords(sorted.entries().stream().limit(10).map(Map.Entry::getValue).toArray(String[]::new));
            }
        }
        this.xOManager.currentTransaction().commit();
        return identifiedGroups;
    }

    private Map<Long, Set<Long>> inititialPartitioningFromPackageStructure(long[] ids) {
        // Package name to type ids
        Map<String, Set<Long>> packageComponents = new HashMap<>();
        TypeRepository typeRepository = this.xOManager.getRepository(TypeRepository.class);
        for (Long id : ids) {
            String packageName = typeRepository.getPackageName(id);
            packageComponents.merge(
                packageName,
                Sets.newHashSet(id),
                (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });
        }
        long componentId = 0;
        Map<Long, Set<Long>> components = new HashMap<>();
        for (Map.Entry<String, Set<Long>> component : packageComponents.entrySet()) {
            components.put(componentId, component.getValue());
            componentId++;
        }
        return components;
    }

    private Map<Long, Set<Long>> initialPartitioningFromComponents(Set<ComponentDescriptor> components, long[] ids) {
        Map<Long, Set<Long>> componentMappings = new HashMap<>();
        ComponentRepository componentRepository = this.xOManager.getRepository(ComponentRepository.class);
        for (Long id : ids) {
            long componentId = 0;
            for (ComponentDescriptor component : components) {
                Long cId = this.xOManager.getId(component);
                if (componentRepository.isCandidateType(cId, id) || componentRepository.isCandidateComponent(cId, id)) {
                    componentMappings.merge(
                        componentId,
                        Sets.newHashSet(id),
                        (t1, t2) -> {
                            t1.addAll(t2);
                            return t1;
                        }
                    );
                    break;
                }
                componentId++;
            }
        }
        return componentMappings;
    }

    private Map<Long, Set<Long>> partitioningFromGroups(Set<Long> elements) {
        Map<Long, Set<Long>> partitioning = new HashMap<>();
        Long cId = 0L;
        for (Long group : elements) {
            partitioning.put(cId, Sets.newHashSet(group));
            cId++;
        }
        return partitioning;
    }
}
