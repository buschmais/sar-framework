package com.buchmais.sarf.classification;

import com.buchmais.sarf.node.ClassificationCriterionDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public class ClassNamingCriterion extends ClassificationCriterion {


    public ClassNamingCriterion(double weight) {
        super(weight);
    }

    @Override
    public Set<ComponentDescriptor> classify() {
        return null;
    }

    @Override
    public ClassificationCriterionDescriptor materialize() {
        return null;
    }
}
