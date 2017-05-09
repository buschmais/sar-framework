package com.buchmais.sarf.classification;

import com.buchmais.sarf.node.ClassificationConfigurationDescriptor;
import com.buchmais.sarf.node.ClassificationCriterionDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public abstract class ClassificationCriterion implements Comparable<ClassificationCriterion> {

    ClassificationCriterionDescriptor classificationCriterionDescriptor;

    Integer weight;

    public abstract Set<ComponentDescriptor> classify();

    public ClassificationCriterionDescriptor getClassificationCriterionDescriptor() {
        if (this.classificationCriterionDescriptor == null) {
            materialize();
        }
        return this.classificationCriterionDescriptor;
    }

    public abstract ClassificationCriterionDescriptor materialize();

    @Override
    public int compareTo(ClassificationCriterion o) {
        int res = getClass().getName().compareTo(o.getClass().getName());
        //todo
        return res;
    }
}
