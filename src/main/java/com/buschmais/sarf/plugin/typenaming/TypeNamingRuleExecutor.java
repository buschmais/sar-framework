package com.buschmais.sarf.plugin.typenaming;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.plugin.api.RuleExecutor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOManager;
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
public class TypeNamingRuleExecutor extends RuleExecutor<TypeNamingRuleDescriptor> {

    public TypeNamingRuleExecutor(XOManager xoManager) {
        super(xoManager);
    }

    @Override
    public Set<TypeDescriptor> getMatchingTypes(TypeNamingRuleDescriptor rule) {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        TypeNamingRepository repository = this.xoManager.getRepository(TypeNamingRepository.class);
        Query.Result<TypeDescriptor> result = repository.getAllInternalTypesByNameLike(rule.getName());
        for (TypeDescriptor t : result) {
            types.add(t);
            // FIXME: 07.07.2017 Cannot create an instance of a single abstract type [interface com.buschmais.xo.api.CompositeObject] types.addAll(t.getDeclaredInnerClasses());
        }
        return types;
    }
}
