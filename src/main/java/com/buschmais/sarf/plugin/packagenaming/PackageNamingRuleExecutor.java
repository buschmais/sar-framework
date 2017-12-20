package com.buschmais.sarf.plugin.packagenaming;

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
public class PackageNamingRuleExecutor extends RuleExecutor<PackageNamingRuleDescriptor> {

    @Autowired
    public PackageNamingRuleExecutor(XOManager xoManager) {
        super(xoManager);
    }

    @Override
    protected Result<TypeDescriptor> getMatchingTypes(PackageNamingRuleDescriptor executableDescriptor) {
        PackageNamingRepository repository = this.xoManager.getRepository(PackageNamingRepository.class);
        return repository.getAllInternalTypesInPackageLike(executableDescriptor.getRule());
    }
}
