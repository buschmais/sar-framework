package com.buschmais.sarf.classification.criterion.cohesion;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.classification.criterion.ClassificationCriterion;
import com.buschmais.sarf.classification.criterion.ClassificationCriterionDescriptor;
import com.buschmais.sarf.classification.criterion.cohesion.evolution.Partitioner;
import com.buschmais.sarf.classification.criterion.cohesion.evolution.Problem;
import com.buschmais.sarf.metamodel.ComponentDescriptor;
import com.buschmais.sarf.repository.ComponentRepository;
import com.buschmais.sarf.repository.TypeRepository;
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
        return classify(iteration, null, 300, 100, true, true);
    }

    public Set<ComponentDescriptor> classify(Integer iteration, Map<Long, Set<Long>> components,
                                             Integer generations, Integer populationSize,
                                             boolean hierarchical, boolean similarityBased) {
        LOG.info("Partitioning the System");
        List<Long> typeIds = new ArrayList<>();
        DatabaseHelper.xoManager.currentTransaction().begin();
        TypeRepository typeRepository = DatabaseHelper.xoManager.getRepository(TypeRepository.class);
        Result<TypeDescriptor> types = typeRepository.getAllInternalTypes();
        for (TypeDescriptor type : types) {
            typeIds.add(DatabaseHelper.xoManager.getId(type));
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
        DatabaseHelper.xoManager.currentTransaction().commit();

        int componentLevel = 0;
        do {
            LOG.info("Computing Level " + componentLevel + " Components");
            createProblem(ids, similarityBased);
            DatabaseHelper.xoManager.currentTransaction().begin();
            Map<Long, Set<Long>> partitioning = Partitioner.partition(ids, initialPartitioning, generations, populationSize, similarityBased);
            DatabaseHelper.xoManager.currentTransaction().commit();
            Set<Long> identifiedGroups = materializeGroups(partitioning, iteration, componentLevel, !hierarchical);
            if (!hierarchical) {
                DatabaseHelper.xoManager.currentTransaction().begin();
                Set<ComponentDescriptor> res = new HashSet<>();
                for (Long id : identifiedGroups) {
                    ComponentDescriptor cD = DatabaseHelper.xoManager.findById(ComponentDescriptor.class, id);
                    res.add(cD);
                }
                DatabaseHelper.xoManager.currentTransaction().commit();
                return res;
            }
            ids = identifiedGroups.stream().mapToLong(l -> l).sorted().toArray();
            DatabaseHelper.xoManager.currentTransaction().begin();
            ComponentRepository componentRepository = DatabaseHelper.xoManager.getRepository(ComponentRepository.class);
            componentRepository.computeCouplingBetweenComponents(ids);
            componentRepository.computeCouplingBetweenComponentsAndTypes(ids);
            componentRepository.computeCouplingBetweenTypesAndComponents(ids);
            componentRepository.computeSimilarityBetweenComponents(ids);
            componentRepository.computeSimilarityBetweenComponentsAndTypes(ids);
            DatabaseHelper.xoManager.currentTransaction().commit();
            initialPartitioning = partitioningFromGroups(identifiedGroups);
            componentLevel++;
        } while (ids.length > 1);
        DatabaseHelper.xoManager.currentTransaction().begin();
        ComponentDescriptor result = DatabaseHelper.xoManager.findById(ComponentDescriptor.class, ids[0]);
        DatabaseHelper.xoManager.currentTransaction().commit();
        LOG.info("Partitioning Finished");
        return Sets.newHashSet(result);
    }

    private Problem createProblem(long[] ids, boolean similarityBased) {
        int maxId = (int) Arrays.stream(ids).max().orElse(0);
        Problem p = Problem.newInstance(maxId + 1, maxId + 1, similarityBased);
        LOG.info("Creating Problem");
        Query<CompositeRowObject> query;
        if (similarityBased) {
            query = DatabaseHelper.xoManager.createQuery(
                    "MATCH\n" +
                            "  (t)-[s:IS_SIMILAR_TO]->(d) \n" +
                            "WHERE\n" +
                            "  ID(t) IN " + Arrays.toString(ids) + " AND ID(d) IN " + Arrays.toString(ids) + "\n" +
                            "RETURN\n" +
                            "  ID(t) AS t, ID(d) AS d, toFloat(s.similarity) AS r");
        } else {
            query = DatabaseHelper.xoManager.createQuery(
                    "MATCH\n" +
                            "  (t)-[c:COUPLES]->(d) \n" +
                            "WHERE\n" +
                            "  ID(t) IN " + Arrays.toString(ids) + " AND ID(d) IN " + Arrays.toString(ids) + "\n" +
                            "RETURN\n" +
                            "  ID(t) AS t, ID(d) AS d, toFloat(c.coupling) AS r");
        }
        try (Result<CompositeRowObject> res = query.execute()) {
            res.forEach(r -> p.addRelation(r.get("t", Long.class).intValue(), r.get("d", Long.class).intValue(), r.get("r", Double.class)));
        }
        LOG.info("Creating Problem Successful");
        return p;
    }

    private Set<Long> materializeGroups(Map<Long, Set<Long>> partitioning, int iteration, int level, boolean typeWrapper) {
        DatabaseHelper.xoManager.currentTransaction().begin();
        Set<Long> identifiedGroups = new HashSet<>();
        ComponentRepository componentRepository = DatabaseHelper.xoManager.getRepository(ComponentRepository.class);
        for (Map.Entry<Long, Set<Long>> component : partitioning.entrySet()) {
            if (component.getValue().size() == 1 && !typeWrapper) {
                identifiedGroups.add(component.getValue().iterator().next());
            } else {
                ComponentDescriptor componentDescriptor = DatabaseHelper.xoManager.create(ComponentDescriptor.class);
                componentDescriptor.setShape("Component");
                componentDescriptor.setName("COH" + iteration + "L" + level + "#" + component.getKey());
                for (Long id : component.getValue()) {
                    try {
                        ComponentDescriptor cD = DatabaseHelper.xoManager.findById(ComponentDescriptor.class, id);
                        componentDescriptor.getContainedComponents().add(cD);
                    } catch (ClassCastException e) {
                        TypeDescriptor tD = DatabaseHelper.xoManager.findById(TypeDescriptor.class, id);
                        componentDescriptor.getContainedTypes().add(tD);
                    }
                }
                identifiedGroups.add(DatabaseHelper.xoManager.getId(componentDescriptor));
                Result<TypeDescriptor> typeDescriptors = componentRepository.getContainedTypesRecursively(DatabaseHelper.xoManager.getId(componentDescriptor));
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
        DatabaseHelper.xoManager.currentTransaction().commit();
        return identifiedGroups;
    }

    private Map<Long, Set<Long>> inititialPartitioningFromPackageStructure(long[] ids) {
        // Package name to type ids
        Map<String, Set<Long>> packageComponents = new HashMap<>();
        TypeRepository typeRepository = DatabaseHelper.xoManager.getRepository(TypeRepository.class);
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
        ComponentRepository componentRepository = DatabaseHelper.xoManager.getRepository(ComponentRepository.class);
        for (Long id : ids) {
            long componentId = 0;
            for (ComponentDescriptor component : components) {
                Long cId = DatabaseHelper.xoManager.getId(component);
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
        return DatabaseHelper.xoManager.create(CohesionCriterionDescriptor.class);
    }

    @Override
    public ClassificationCriterionDescriptor materialize() {
        DatabaseHelper.xoManager.currentTransaction().begin();
        CohesionCriterionDescriptor descriptor = instantiateDescriptor();
        DatabaseHelper.xoManager.currentTransaction().commit();
        this.classificationCriterionDescriptor = descriptor;
        return descriptor;
    }
}
