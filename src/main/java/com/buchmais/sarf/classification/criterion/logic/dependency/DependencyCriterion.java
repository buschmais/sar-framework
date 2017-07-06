package com.buchmais.sarf.classification.criterion.logic.dependency;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.data.node.dependency.DependencyCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.logic.RuleBasedCriterion;

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
