package com.buchmais.sarf.classification;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.node.ClassificationConfigurationDescriptor;
import com.buchmais.sarf.node.ClassificationCriterionDescriptor;
import lombok.Getter;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
public abstract class ClassificationConfiguration {

    @Getter
    Integer iteration;

    @Getter
    Set<ClassificationCriterion> classificationCriteria;

    private ClassificationConfigurationDescriptor classificationConfigurationDescriptor;

    ClassificationConfiguration(Integer iteration) {
        this.iteration = iteration;
        this.classificationCriteria = new TreeSet<>();
    }

    public boolean addClassificationCriterion(ClassificationCriterion classificationCriterion) {
        return this.classificationCriteria.add(classificationCriterion);
    }

    public boolean addAllClassificationCriterion(Set<ClassificationCriterion> classificationCriteria) {
        return this.classificationCriteria.addAll(classificationCriteria);
    }

    public ClassificationConfigurationDescriptor materialize() {
        Set<ClassificationCriterionDescriptor> descriptors = this.classificationCriteria.stream().map(ClassificationCriterion::materialize).collect(Collectors.toSet());
        SARFRunner.xoManager.currentTransaction().begin();
        if (this.classificationConfigurationDescriptor == null) {
            this.classificationConfigurationDescriptor = SARFRunner.xoManager.create(ClassificationConfigurationDescriptor.class);
            this.classificationConfigurationDescriptor.setIteration(this.iteration);
            this.classificationConfigurationDescriptor.getClassificationCriteria().addAll(descriptors);
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return this.classificationConfigurationDescriptor;
    }

    public abstract ClassificationConfiguration of(ClassificationConfigurationDescriptor classificationConfigurationDescriptor);
}
