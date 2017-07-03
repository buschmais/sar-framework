package com.buchmais.sarf.classification.criterion.cohesion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.ClassificationCriterion;
import com.buchmais.sarf.classification.criterion.cohesion.evolution.Partitioner;
import com.buchmais.sarf.node.ClassificationCriterionDescriptor;
import com.buchmais.sarf.node.ClassificationInfoDescriptor;
import com.buchmais.sarf.node.CohesionCriterionDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Stephan Pirnbaum
 */
public class CohesionCriterion extends ClassificationCriterion<CohesionCriterionDescriptor> {

    @Override
    public Set<ComponentDescriptor> classify(Integer iteration) {
        return classify(iteration, null);
    }

    public Set<ComponentDescriptor> classify(Integer iteration, Set<ComponentDescriptor> components) {
        Partitioner partitioner = new Partitioner();
        Map<Long, Set<Long>> partitioning = partitioner.partition(components);
        Set<ComponentDescriptor> componentDescriptors = new TreeSet<>();
        SARFRunner.xoManager.currentTransaction().begin();
        for (Map.Entry<Long, Set<Long>> component : partitioning.entrySet()) {
            ComponentDescriptor componentDescriptor = SARFRunner.xoManager.create(ComponentDescriptor.class);
            componentDescriptor.setShape("Component");
            componentDescriptor.setName("COH" + iteration + "#" + component.getKey());
            for (Long id : component.getValue()) {
                ClassificationInfoDescriptor classificationInfoDescriptor = SARFRunner.xoManager.create(ClassificationInfoDescriptor.class);
                classificationInfoDescriptor.setIteration(iteration);
                classificationInfoDescriptor.setComponent(componentDescriptor);
                classificationInfoDescriptor.setType(SARFRunner.xoManager.findById(TypeDescriptor.class, id));
            }
            componentDescriptors.add(componentDescriptor);

        }
        SARFRunner.xoManager.currentTransaction().commit();
        return componentDescriptors;
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
