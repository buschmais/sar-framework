package com.buchmais.sarf.classification.criterion.cohesion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.ClassificationCriterion;
import com.buchmais.sarf.classification.criterion.ClassificationCriterionDescriptor;
import com.buchmais.sarf.metamodel.ComponentDescriptor;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.google.common.collect.Sets;
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
        return classify(iteration, null, true);
    }

    public Set<ComponentDescriptor> classify(Integer iteration, Map<Long, Set<Long>> components, boolean hierarchical) {
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
        int iterations = 500;
        do {
            LOG.info("Computing Level " + componentLevel + " Components");
            Map<Long, Set<Long>> partitioning = Partitioner.partition(ids, initialPartitioning, iterations);
            Set<Long> identifiedGroups = materializeGroups(partitioning, iteration, componentLevel);
            if (!hierarchical) {
                SARFRunner.xoManager.currentTransaction().begin();
                Set<ComponentDescriptor> res = new HashSet<>();
                for (Long id : identifiedGroups) {
                    try {
                        ComponentDescriptor cD = SARFRunner.xoManager.findById(ComponentDescriptor.class, id);
                        res.add(cD);
                    } catch (ClassCastException e) {
                        ComponentDescriptor cD = SARFRunner.xoManager.create(ComponentDescriptor.class);
                        cD.setShape("Component");
                        cD.setName("COH" + iteration + "L" + componentLevel + "#" + (-id));
                        cD.getContainedTypes().add(SARFRunner.xoManager.findById(TypeDescriptor.class, id));
                        res.add(cD);
                    }
                }
                SARFRunner.xoManager.currentTransaction().commit();
                return res;
            }
            ids = identifiedGroups.stream().mapToLong(l -> l).sorted().toArray();
            SARFRunner.xoManager.currentTransaction().begin();
            ComponentRepository componentRepository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
            componentRepository.computeCouplingBetweenComponents(ids);
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

    private Set<Long> materializeGroups(Map<Long, Set<Long>> partitioning, int iteration, int level) {
        SARFRunner.xoManager.currentTransaction().begin();
        Set<Long> identifiedGroups = new HashSet<>();
        for (Map.Entry<Long, Set<Long>> component : partitioning.entrySet()) {
            if (component.getValue().size() > 1) {
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
            } else if (component.getValue().size() == 1) {
                identifiedGroups.add(component.getValue().iterator().next());
            }
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
