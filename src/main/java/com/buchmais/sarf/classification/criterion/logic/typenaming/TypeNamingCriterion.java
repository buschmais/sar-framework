package com.buchmais.sarf.classification.criterion.logic.typenaming;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.data.node.typenaming.ClassNamingCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.logic.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class TypeNamingCriterion extends RuleBasedCriterion<TypeNamingRule, ClassNamingCriterionDescriptor> {

    @Override
    protected ClassNamingCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(ClassNamingCriterionDescriptor.class);
    }
}
