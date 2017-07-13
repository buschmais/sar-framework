package com.buchmais.sarf.classification.criterion.packagenaming;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class PackageNamingCriterion extends RuleBasedCriterion<PackageNamingRule, PackageNamingCriterionDescriptor> {

    @Override
    protected PackageNamingCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(PackageNamingCriterionDescriptor.class);
    }
}
