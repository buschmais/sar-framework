package com.buschmais.sarf.plugin.packagenaming;

import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.plugin.api.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class PackageNamingCriterion extends RuleBasedCriterion<PackageNamingRule, PackageNamingCriterionDescriptor> {

    @Override
    protected PackageNamingCriterionDescriptor instantiateDescriptor() {
        return DatabaseHelper.xoManager.create(PackageNamingCriterionDescriptor.class);
    }
}
