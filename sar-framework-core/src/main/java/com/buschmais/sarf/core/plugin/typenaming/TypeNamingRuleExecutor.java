package com.buschmais.sarf.core.plugin.typenaming;

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
public class TypeNamingRuleExecutor extends RuleExecutor<TypeNamingRuleDescriptor> {

    private final TypeNamingRepository typeNamingRepository;

    public TypeNamingRuleExecutor(XOManager xoManager, TypeNamingRepository typeNamingRepository) {
        super(xoManager);
        this.typeNamingRepository = typeNamingRepository;
    }

    @Override
    protected Result<TypeDescriptor> getMatchingTypes(TypeNamingRuleDescriptor executableDescriptor) {
        return this.typeNamingRepository.getAllInternalTypesByNameLike(executableDescriptor.getRule());
    }


}
