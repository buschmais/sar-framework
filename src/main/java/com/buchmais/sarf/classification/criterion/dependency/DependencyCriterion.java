package com.buchmais.sarf.classification.criterion.dependency;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterion;
import com.buchmais.sarf.node.DependencyCriterionDescriptor;

/**
 * @author Stephan Pirnbaum
 */
public class DependencyCriterion extends RuleBasedCriterion<DependencyRule, DependencyCriterionDescriptor> {

    @Override
    protected DependencyCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(DependencyCriterionDescriptor.class);
    }
/*
    public static DependencyCriterion of(DependencyCriterionDescriptor dependencyCriterionDescriptor) {
        DependencyCriterion dependencyCriterion = new DependencyCriterion(dependencyCriterionDescriptor.getWeight());
        for (DependencyDescriptor dependencyDescriptor : dependencyCriterionDescriptor.getRules()) {
            dependencyCriterion.addRule(Dependency.of(dependencyDescriptor));
        }
        return dependencyCriterion;
    }
*/
}
