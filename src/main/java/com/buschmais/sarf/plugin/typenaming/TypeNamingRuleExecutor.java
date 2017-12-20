package com.buschmais.sarf.plugin.typenaming;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.plugin.api.criterion.RuleExecutor;
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

    public TypeNamingRuleExecutor(XOManager xoManager) {
        super(xoManager);
    }

    @Override
    protected Result<TypeDescriptor> getMatchingTypes(TypeNamingRuleDescriptor executableDescriptor) {
        TypeNamingRepository repository = this.xoManager.getRepository(TypeNamingRepository.class);
        return repository.getAllInternalTypesByNameLike(executableDescriptor.getRule());
    }


}
