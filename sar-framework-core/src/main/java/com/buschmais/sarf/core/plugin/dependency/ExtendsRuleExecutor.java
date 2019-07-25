package com.buschmais.sarf.core.plugin.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.XOManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
public class ExtendsRuleExecutor extends DependencyRuleExecutor<ExtendsRuleDescriptor> {

    public ExtendsRuleExecutor(XOManager xoManager, DependencyRepository dependencyRepository) {
        super(xoManager, dependencyRepository);
    }

    @Override
    protected Result<TypeDescriptor> getMatchingTypes( ExtendsRuleDescriptor rule) {
        return this.dependencyRepository.getAllInternalTypesExtending(rule.getRule());
    }
}
