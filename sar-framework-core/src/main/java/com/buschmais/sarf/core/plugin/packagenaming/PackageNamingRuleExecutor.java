package com.buschmais.sarf.core.plugin.packagenaming;

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
public class PackageNamingRuleExecutor extends RuleExecutor<PackageNamingRuleDescriptor> {

    private final PackageNamingRepository packageNamingRepository;

    public PackageNamingRuleExecutor(XOManager xoManager, PackageNamingRepository packageNamingRepository) {
        super(xoManager);
        this.packageNamingRepository = packageNamingRepository;
    }

    @Override
    protected Result<TypeDescriptor> getMatchingTypes(PackageNamingRuleDescriptor executableDescriptor) {
        return this.packageNamingRepository.getAllInternalTypesInPackageLike(executableDescriptor.getRule());
    }
}
