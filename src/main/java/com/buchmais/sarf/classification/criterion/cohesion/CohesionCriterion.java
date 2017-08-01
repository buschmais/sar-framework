package com.buchmais.sarf.classification.criterion.cohesion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.ClassificationCriterion;
import com.buchmais.sarf.classification.criterion.ClassificationCriterionDescriptor;
import com.buchmais.sarf.metamodel.ComponentDescriptor;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
import com.google.common.collect.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * @author Stephan Pirnbaum
 */
public class CohesionCriterion extends ClassificationCriterion<CohesionCriterionDescriptor> {

    private static final Logger LOG = LogManager.getLogger(CohesionCriterion.class);

    @Override
    public Set<ComponentDescriptor> classify(Integer iteration) {
        return classify(iteration, null, true, true);
    }

    public Set<ComponentDescriptor> classify(Integer iteration, Map<Long, Set<Long>> components, boolean hierarchical, boolean similarityBased) {
        LOG.info("Partitioning the System");
        List<Long> typeIds = new ArrayList<>();
        SARFRunner.xoManager.currentTransaction().begin();
        TypeRepository typeRepository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        Result<TypeDescriptor> types = typeRepository.getAllInternalTypes();
        for (TypeDescriptor type : types) {
            typeIds.add(SARFRunner.xoManager.getId(type));
        }
        types.close();
        long[] ids = typeIds.stream().mapToLong(l -> l).sorted().toArray();
        Problem problem = createProblem(ids);
        // create initial partitioning
        Map<Long, Set<Long>> initialPartitioning;
        if (components != null && components.size() > 0) {
            //initialPartitioning = initialPartitioningFromComponents(components, ids);
            initialPartitioning = components;
        } else {
            initialPartitioning = inititialPartitioningFromPackageStructure(ids);
        }
        SARFRunner.xoManager.currentTransaction().commit();

        int componentLevel = 0;
        int iterations = 300;
        do {
            LOG.info("Computing Level " + componentLevel + " Components");
            Map<Long, Set<Long>> partitioning = Partitioner.partition(ids, initialPartitioning, iterations, similarityBased);
            Set<Long> identifiedGroups = materializeGroups(partitioning, iteration, componentLevel);
            if (!hierarchical) {
                SARFRunner.xoManager.currentTransaction().begin();
                Set<ComponentDescriptor> res = new HashSet<>();
                for (Long id : identifiedGroups) {
                    ComponentDescriptor cD = SARFRunner.xoManager.findById(ComponentDescriptor.class, id);
                    res.add(cD);
                }
                SARFRunner.xoManager.currentTransaction().commit();
                return res;
            }
            ids = identifiedGroups.stream().mapToLong(l -> l).sorted().toArray();
            SARFRunner.xoManager.currentTransaction().begin();
            ComponentRepository componentRepository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
            componentRepository.computeCouplingBetweenComponents(ids);
            componentRepository.computeSimilarityBetweenComponents(ids);
            SARFRunner.xoManager.currentTransaction().commit();
            initialPartitioning = partitioningFromGroups(identifiedGroups);
            componentLevel++;
            iterations = 100;
        } while (ids.length > 1);
        SARFRunner.xoManager.currentTransaction().begin();
        ComponentDescriptor result = SARFRunner.xoManager.findById(ComponentDescriptor.class, ids[0]);
        SARFRunner.xoManager.currentTransaction().commit();
        LOG.info("Partitioning Finished");
        return Sets.newHashSet(result);
    }

    private Problem createProblem(long[] ids) {
        Map<Long, Node> nodes = new HashMap<>();
        for (long id : ids) {
            nodes.put(id, new Node(id));
        }
        for (Long id : ids) {
            Node node = nodes.get(id);
            Query<CompositeRowObject> q1 = SARFRunner.xoManager.createQuery(
                    "MATCH\n" +
                    "  (t:Type:Internal)-[c:COUPLES]->(d:Type:Internal) \n" +
                    "WHERE\n" +
                    "  ID(t) = " + id + " AND ID(d) IN " + Arrays.toString(ids) + "\n" +
                    "RETURN\n" +
                    "  ID(d) AS d, toFloat(c.coupling) AS c");
            try (Result<CompositeRowObject> res = q1.execute()) {
                res.forEach(r -> node.addCoupling(nodes.get(r.get("d", Long.class)), r.get("c", Double.class)));
            }
            Query<CompositeRowObject> q2 = SARFRunner.xoManager.createQuery(
                    "MATCH\n" +
                    "  (t:Type:Internal)-[s:IS_SIMILAR_TO]->(d:Type:Internal) \n" +
                    "WHERE\n" +
                    "  ID(t) = " + id + " AND ID(d) IN " + Arrays.toString(ids) + "\n" +
                    "RETURN\n" +
                    "  ID(d) AS d, toFloat(s.similarity) AS s"
            );
            try (Result<CompositeRowObject> res = q2.execute()) {
                res.forEach(r -> node.addSimilarity(nodes.get(r.get("d", Long.class)), r.get("s", Double.class)));
            }
        }
        return Problem.newInstance(Sets.newHashSet(nodes.values()));
    }

    private Set<Long> materializeGroups(Map<Long, Set<Long>> partitioning, int iteration, int level) {
        SARFRunner.xoManager.currentTransaction().begin();
        Set<Long> identifiedGroups = new HashSet<>();
        ComponentRepository componentRepository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
        for (Map.Entry<Long, Set<Long>> component : partitioning.entrySet()) {
            ComponentDescriptor componentDescriptor = SARFRunner.xoManager.create(ComponentDescriptor.class);
            componentDescriptor.setShape("Component");
            componentDescriptor.setName("COH" + iteration + "L" + level + "#" + component.getKey());
            for (Long id : component.getValue()) {
                try {
                    ComponentDescriptor cD = SARFRunner.xoManager.findById(ComponentDescriptor.class, id);
                    componentDescriptor.getContainedComponents().add(cD);
                } catch (ClassCastException e) {
                    TypeDescriptor tD = SARFRunner.xoManager.findById(TypeDescriptor.class, id);
                    componentDescriptor.getContainedTypes().add(tD);
                }
            }
            identifiedGroups.add(SARFRunner.xoManager.getId(componentDescriptor));
            Result<TypeDescriptor> typeDescriptors = componentRepository.getContainedTypesRecursively(SARFRunner.xoManager.getId(componentDescriptor));
            Map<String, Long> wordCount = new HashMap<>();
            for (TypeDescriptor typeDescriptor : typeDescriptors) {
                String[] words = StringUtils.splitByCharacterTypeCamelCase(typeDescriptor.getName());
                for (String word : words) {
                    wordCount.merge(
                            word,
                            1L,
                            (w1, w2) -> w1 + 1
                    );
                }
            }
            ListMultimap<Long, String> sorted = new ImmutableListMultimap.Builder<Long, String>()
                    .orderKeysBy(Ordering.natural().reverse())
                    .putAll(Multimaps.invertFrom(Multimaps.forMap(wordCount), ArrayListMultimap.create()))
                    .build();
            componentDescriptor.setTopWords(sorted.entries().stream().limit(10).map(Map.Entry::getValue).toArray(String[]::new));
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return identifiedGroups;
    }

    private Map<Long, Set<Long>> inititialPartitioningFromPackageStructure(long[] ids) {
        // Package name to type ids
        Map<String, Set<Long>> packageComponents = new HashMap<>();
        TypeRepository typeRepository = SARFRunner.xoManager.getRepository(TypeRepository.class);
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
        ComponentRepository componentRepository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
        for (Long id : ids) {
            long componentId = 0;
            for (ComponentDescriptor component : components) {
                Long cId = SARFRunner.xoManager.getId(component);
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

    @Override
    protected CohesionCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(CohesionCriterionDescriptor.class);
    }

    @Override
    public ClassificationCriterionDescriptor materialize() {
        SARFRunner.xoManager.currentTransaction().begin();
        CohesionCriterionDescriptor descriptor = instantiateDescriptor();
        SARFRunner.xoManager.currentTransaction().commit();
        this.classificationCriterionDescriptor = descriptor;
        return descriptor;
    }
}
