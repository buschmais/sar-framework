package com.buchmais.sarf.classification.criterion.dependency;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterion;
import com.buchmais.sarf.node.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@XmlRootElement(name = "DependencyCriterion")
public class DependencyCriterion extends RuleBasedCriterion<DependencyRule, DependencyCriterionDescriptor> {

    public DependencyCriterion(double weight) {
        super(weight);
    }

    @Override
    protected DependencyCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(DependencyCriterionDescriptor.class);
    }
/*
    public static DependencyCriterion of(DependencyCriterionDescriptor dependencyCriterionDescriptor) {
        DependencyCriterion dependencyCriterion = new DependencyCriterion(dependencyCriterionDescriptor.getWeight());
        for (DependencyDescriptor dependencyDescriptor : dependencyCriterionDescriptor.getRules()) {
            dependencyCriterion.addRule(Dependency.of(dependencyDescriptor));
        }
        return dependencyCriterion;
    }
*/
}
