package com.buschmais.sarf.plugin.dependency;

import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.plugin.api.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class DependencyCriterion extends RuleBasedCriterion<DependencyRule, DependencyCriterionDescriptor> {

    @Override
    protected DependencyCriterionDescriptor instantiateDescriptor() {
        return DatabaseHelper.xoManager.create(DependencyCriterionDescriptor.class);
    }
}
