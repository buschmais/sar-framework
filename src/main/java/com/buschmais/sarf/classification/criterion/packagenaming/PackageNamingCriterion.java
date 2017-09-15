package com.buschmais.sarf.classification.criterion.packagenaming;

import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.classification.criterion.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class PackageNamingCriterion extends RuleBasedCriterion<PackageNamingRule, PackageNamingCriterionDescriptor> {

    @Override
    protected PackageNamingCriterionDescriptor instantiateDescriptor() {
        return DatabaseHelper.xoManager.create(PackageNamingCriterionDescriptor.class);
    }
}
