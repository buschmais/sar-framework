package com.buschmais.sarf.classification.criterion.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.SARFRunner;
import com.buschmais.xo.api.Query.Result;

/**
 * @author Stephan Pirnbaum
 */
public class ImplementsRule extends DependencyRule<ImplementsRule, ImplementsDescriptor> {

    @Override
    Result<TypeDescriptor> getMatchingTypes(DependencyRepository repository) {
        return repository.getAllInternalTypesImplementing(this.rule);
    }

    @Override
    protected ImplementsDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(ImplementsDescriptor.class);
    }
}