package com.buchmais.sarf.classification.criterion.typenaming;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterion;
import com.buchmais.sarf.node.ClassNamingCriterionDescriptor;

/**
 * @author Stephan Pirnbaum
 */
public class TypeNamingCriterion extends RuleBasedCriterion<TypeNamingRule, ClassNamingCriterionDescriptor> {

    @Override
    protected ClassNamingCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(ClassNamingCriterionDescriptor.class);
    }
}
