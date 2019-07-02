package com.buschmais.sarf.plugin.dependency;

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
public class AnnotatedByRuleExecutor extends DependencyRuleExecutor<AnnotatedByRuleDescriptor> {

    public AnnotatedByRuleExecutor(XOManager xoManager, DependencyRepository dependencyRepository) {
        super(xoManager, dependencyRepository);
    }

    @Override
    protected Result<TypeDescriptor> getMatchingTypes(AnnotatedByRuleDescriptor rule) {
        return this.dependencyRepository.getAllInternalTypesAnnotatedBy(rule.getRule());
    }
}
