package com.buschmais.sarf.classification.criterion.dependency;

import com.buschmais.sarf.SARFRunner;
import com.buschmais.sarf.classification.criterion.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class DependencyCriterion extends RuleBasedCriterion<DependencyRule, DependencyCriterionDescriptor> {

    @Override
    protected DependencyCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(DependencyCriterionDescriptor.class);
    }
}
