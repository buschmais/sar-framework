package com.buschmais.sarf.core.plugin.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.plugin.api.criterion.RuleExecutor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.XOManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
public class DependencyRuleExecutor<R extends DependencyRuleDescriptor> extends RuleExecutor<R> {

    final DependencyRepository dependencyRepository;

    public DependencyRuleExecutor(XOManager xoManager, DependencyRepository dependencyRepository) {
        super(xoManager);
        this.dependencyRepository = dependencyRepository;
    }

    @Override
    protected Result<TypeDescriptor> getMatchingTypes(R executableDescriptor) {
        return this.dependencyRepository.getAllInternalTypesDependingOn(executableDescriptor.getRule());
    }
}
