package com.buchmais.sarf.classification.criterion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.Pattern;
import com.buchmais.sarf.node.ClassNamingCriterionDescriptor;
import com.buchmais.sarf.node.ClassificationCriterionDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ClassNamingCriterion extends RuleBasedCriterion<Pattern> {


    public ClassNamingCriterion(double weight) {
        super(weight);
    }

    @Override
    public ClassNamingCriterionDescriptor materialize() {
        SARFRunner.xoManager.currentTransaction().begin();
        ClassNamingCriterionDescriptor descriptor = SARFRunner.xoManager.create(ClassNamingCriterionDescriptor.class);
        descriptor.getPatterns().addAll(
                this.rules.stream().map(Pattern::getDescriptor).collect(Collectors.toSet())
        );
        SARFRunner.xoManager.currentTransaction().commit();
        this.classificationCriterionDescriptor = descriptor;
        return descriptor;
    }
}
