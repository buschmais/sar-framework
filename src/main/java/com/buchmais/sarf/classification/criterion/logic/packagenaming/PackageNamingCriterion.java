package com.buchmais.sarf.classification.criterion.logic.packagenaming;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.data.node.packagenaming.PackageNamingCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.logic.RuleBasedCriterion;

/**
 * @author Stephan Pirnbaum
 */
public class PackageNamingCriterion extends RuleBasedCriterion<PackageNamingRule, PackageNamingCriterionDescriptor> {

    @Override
    protected PackageNamingCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(PackageNamingCriterionDescriptor.class);
    }
/*
    public static PackageNamingCriterion of(PackageNamingCriterionDescriptor packageNamingCriterionDescriptor) {
        PackageNamingCriterion packageNamingCriterion = new PackageNamingCriterion(packageNamingCriterionDescriptor.getWeight());
        for (PatternDescriptor patternDescriptor : packageNamingCriterionDescriptor.getRules()) {
            packageNamingCriterion.addRule(Pattern.of(patternDescriptor));
        }
        return packageNamingCriterion;
    }
*/
}
