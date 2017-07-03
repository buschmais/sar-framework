package com.buchmais.sarf.classification.criterion;

import com.buchmais.sarf.classification.Materializable;
import com.buchmais.sarf.node.ClassificationCriterionDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public abstract class ClassificationCriterion<T extends ClassificationCriterionDescriptor> implements Comparable<ClassificationCriterion>, Materializable<ClassificationCriterionDescriptor> {

    protected T classificationCriterionDescriptor;

    public abstract Set<ComponentDescriptor> classify(Integer iteration);

    public T getClassificationCriterionDescriptor() {
        if (this.classificationCriterionDescriptor == null) {
            materialize();
        }
        return this.classificationCriterionDescriptor;
    }

    @Override
    public int compareTo(ClassificationCriterion o) {
        int res = getClass().getName().compareTo(o.getClass().getName());
        //todo
        return res;
    }

    protected abstract T instantiateDescriptor();

    public abstract ClassificationCriterionDescriptor materialize();
}
