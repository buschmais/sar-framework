package com.buschmais.sarf.plugin.typenaming;

import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.plugin.api.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class TypeNamingCriterion extends RuleBasedCriterion<TypeNamingRule, TypeNamingCriterionDescriptor> {

    @Override
    protected TypeNamingCriterionDescriptor instantiateDescriptor() {
        return DatabaseHelper.xoManager.create(TypeNamingCriterionDescriptor.class);
    }
}
