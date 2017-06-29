package com.buchmais.sarf.classification.criterion.typenaming;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.Pattern;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterion;
import com.buchmais.sarf.node.ClassNamingCriterionDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class TypeNamingCriterion extends RuleBasedCriterion<Pattern, ClassNamingCriterionDescriptor> {


    public TypeNamingCriterion(double weight) {
        super(weight);
    }

    @Override
    protected ClassNamingCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(ClassNamingCriterionDescriptor.class);
    }
}
