package com.buschmais.sarf.plugin.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
public class ImplementsRuleExecutor extends DependencyRuleExecutor<ImplementsRuleDescriptor> {

    @Autowired
    public ImplementsRuleExecutor(XOManager xoManager) {
        super(xoManager);
    }

    @Override
    Query.Result<TypeDescriptor> getMatchingTypes(DependencyRepository repository, ImplementsRuleDescriptor rule) {
        return repository.getAllInternalTypesImplementing(rule.getRule());
    }
}
