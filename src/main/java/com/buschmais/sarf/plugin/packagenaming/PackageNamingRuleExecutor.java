package com.buschmais.sarf.plugin.packagenaming;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.plugin.api.RuleDescriptor;
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
public class PackageNamingRuleExecutor extends RuleExecutor<RuleDescriptor> {

    @Autowired
    public PackageNamingRuleExecutor(XOManager xoManager) {
        super(xoManager);
    }

    @Override
    public Set<TypeDescriptor> getMatchingTypes(RuleDescriptor rule) {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        PackageNamingRepository repository = this.xoManager.getRepository(PackageNamingRepository.class);
        Query.Result<TypeDescriptor> result = repository.getAllInternalTypesInPackageLike(rule.getRule());
        for (TypeDescriptor t : result) {
            types.add(t);
        }
        return types;
    }
}
