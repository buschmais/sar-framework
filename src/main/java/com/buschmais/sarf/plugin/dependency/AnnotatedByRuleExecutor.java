package com.buschmais.sarf.plugin.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
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
public class AnnotatedByRuleExecutor extends DependencyRuleExecutor<AnnotatedByRuleDescriptor> {

    @Autowired
    public AnnotatedByRuleExecutor(XOManager xoManager) {
        super(xoManager);
    }

    @Override
    protected Result<TypeDescriptor> getMatchingTypes(AnnotatedByRuleDescriptor rule) {
        DependencyRepository repository = this.xoManager.getRepository(DependencyRepository.class);
        return repository.getAllInternalTypesAnnotatedBy(rule.getRule());
    }
}
