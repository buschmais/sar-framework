package com.buschmais.sarf.classification.criterion.packagenaming;

import com.buschmais.sarf.SARFRunner;
import com.buschmais.sarf.classification.criterion.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class PackageNamingCriterion extends RuleBasedCriterion<PackageNamingRule, PackageNamingCriterionDescriptor> {

    @Override
    protected PackageNamingCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(PackageNamingCriterionDescriptor.class);
    }
}
