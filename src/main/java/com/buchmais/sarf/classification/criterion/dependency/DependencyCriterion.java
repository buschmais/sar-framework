package com.buchmais.sarf.classification.criterion.dependency;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.logic.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class DependencyCriterion extends RuleBasedCriterion<DependencyRule, DependencyCriterionDescriptor> {

    @Override
    protected DependencyCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(DependencyCriterionDescriptor.class);
    }
}
