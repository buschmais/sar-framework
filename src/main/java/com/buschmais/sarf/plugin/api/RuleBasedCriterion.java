package com.buschmais.sarf.plugin.api;

/**
 * @author Stephan Pirnbaum
 */
public abstract class RuleBasedCriterion<R extends RuleDescriptor, T extends RuleBasedCriterionDescriptor> extends ClassificationCriterion<T> {



    @Override
    public T materialize() {
        /*DatabaseHelper.xoManager.currentTransaction().begin();
        T descriptor = instantiateDescriptor();
        descriptor.getRules().addAll(
                this.rules.stream().map(R::getDescriptor).collect(Collectors.toSet())
        );
        DatabaseHelper.xoManager.currentTransaction().commit();
        this.classificationCriterionDescriptor = descriptor;
        return descriptor;*/
        return null;
    }
}
