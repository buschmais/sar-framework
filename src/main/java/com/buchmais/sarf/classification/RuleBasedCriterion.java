package com.buchmais.sarf.classification;

import com.buchmais.sarf.node.ComponentDescriptor;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Stephan Pirnbaum
 */
public abstract class RuleBasedCriterion<T extends Rule> extends ClassificationCriterion {

    Set<T> rules;

    public RuleBasedCriterion(double weight) {
        super(weight);
        this.rules = new TreeSet<>();
    }

    public boolean addRule(T rule) {
        return this.rules.add(rule);
    }

    @Override
    public Set<ComponentDescriptor> classify() {
        return null;
    }
}
