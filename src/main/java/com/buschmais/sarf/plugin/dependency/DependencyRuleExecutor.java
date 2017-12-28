package com.buschmais.sarf.plugin.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.plugin.api.criterion.RuleExecutor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.XOManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
public class DependencyRuleExecutor<R extends DependencyRuleDescriptor> extends RuleExecutor<R> {

    @Autowired
    public DependencyRuleExecutor(XOManager xoManager) {
        super(xoManager);
    }

    @Override
    protected Result<TypeDescriptor> getMatchingTypes(R executableDescriptor) {
        DependencyRepository repository = this.xoManager.getRepository(DependencyRepository.class);
        return repository.getAllInternalTypesDependingOn(executableDescriptor.getRule());
    }
}
