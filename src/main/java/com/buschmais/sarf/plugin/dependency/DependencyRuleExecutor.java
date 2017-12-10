package com.buschmais.sarf.plugin.dependency;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.plugin.api.RuleExecutor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

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
    public Set<TypeDescriptor> getMatchingTypes(R rule) {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        DependencyRepository repository = this.xoManager.getRepository(DependencyRepository.class);
        Query.Result<TypeDescriptor> result = getMatchingTypes(repository, rule);
        for (TypeDescriptor t : result) {
            types.add(t);
            // FIXME: 07.07.2017 Cannot create an instance of a single abstract type [interface com.buschmais.xo.api.CompositeObject] types.addAll(t.getDeclaredInnerClasses());
        }
        return types;
    }

    Query.Result<TypeDescriptor> getMatchingTypes(DependencyRepository repository, R rule) {
        return repository.getAllInternalTypesDependingOn(rule.getRule());
    }
}
