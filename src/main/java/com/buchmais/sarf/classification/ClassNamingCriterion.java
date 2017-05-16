package com.buchmais.sarf.classification;

import com.buchmais.sarf.node.ClassificationCriterionDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ClassNamingCriterion extends RuleBasedCriterion<Pattern> {


    public ClassNamingCriterion(double weight) {
        super(weight);
    }

    @Override
    public Set<ComponentDescriptor> classify() {
        return null;
    }

    @Override
    public ClassificationCriterionDescriptor materialize() {
        return null;
    }
}
