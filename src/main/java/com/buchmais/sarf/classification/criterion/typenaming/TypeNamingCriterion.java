package com.buchmais.sarf.classification.criterion.typenaming;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class TypeNamingCriterion extends RuleBasedCriterion<TypeNamingRule, TypeNamingCriterionDescriptor> {

    @Override
    protected TypeNamingCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(TypeNamingCriterionDescriptor.class);
    }
}
