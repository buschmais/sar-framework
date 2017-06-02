package com.buchmais.sarf.classification.criterion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.Dependency;
import com.buchmais.sarf.classification.Pattern;
import com.buchmais.sarf.node.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@XmlRootElement(name = "DependencyCriterion")
public class DependencyCriterion extends RuleBasedCriterion<Dependency, DependencyCriterionDescriptor> {

    public DependencyCriterion(double weight) {
        super(weight);
    }

    @Override
    DependencyCriterionDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(DependencyCriterionDescriptor.class);
    }

    public static DependencyCriterion of(DependencyCriterionDescriptor dependencyCriterionDescriptor) {
        DependencyCriterion dependencyCriterion = new DependencyCriterion(dependencyCriterionDescriptor.getWeight());
        for (DependencyDescriptor dependencyDescriptor : dependencyCriterionDescriptor.getRules()) {
            dependencyCriterion.addRule(Dependency.of(dependencyDescriptor));
        }
        return dependencyCriterion;
    }

}
