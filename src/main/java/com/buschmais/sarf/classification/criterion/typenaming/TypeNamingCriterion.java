package com.buschmais.sarf.classification.criterion.typenaming;

import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.classification.criterion.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class TypeNamingCriterion extends RuleBasedCriterion<TypeNamingRule, TypeNamingCriterionDescriptor> {

    @Override
    protected TypeNamingCriterionDescriptor instantiateDescriptor() {
        return DatabaseHelper.xoManager.create(TypeNamingCriterionDescriptor.class);
    }
}
