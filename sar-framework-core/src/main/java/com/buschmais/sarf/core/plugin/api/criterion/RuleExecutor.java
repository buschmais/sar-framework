package com.buschmais.sarf.core.plugin.api.criterion;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.plugin.api.Executor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.XOManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Stephan Pirnbaum
 */
@Component
@Lazy
@RequiredArgsConstructor
public abstract class RuleExecutor<E extends RuleDescriptor> implements Executor<E, TypeDescriptor> {

    protected final XOManager xoManager;

    @Override
    public Set<TypeDescriptor> execute(E executableDescriptor) {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        Result<TypeDescriptor> result = getMatchingTypes(executableDescriptor);
        for (TypeDescriptor t : result) {
            types.add(t);
            // FIXME: 07.07.2017 Cannot create an instance of a single abstract type [interface com.buschmais.xo.api.CompositeObject] types.addAll(t.getDeclaredInnerClasses());
        }
        return types;
    }

    protected abstract Result<TypeDescriptor> getMatchingTypes(E executableDescriptor);

}
