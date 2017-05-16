package com.buchmais.sarf.classification.criterion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.Pattern;
import com.buchmais.sarf.node.PackageNamingCriterionDescriptor;
import com.buchmais.sarf.node.PatternDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@XmlRootElement(name = "PackageNamingCriterion")
public class PackageNamingCriterion extends RuleBasedCriterion<Pattern> {

    public PackageNamingCriterion(double weight) {
        super(weight);
    }

    public static PackageNamingCriterion of(PackageNamingCriterionDescriptor packageNamingCriterionDescriptor) {
        PackageNamingCriterion packageNamingCriterion = new PackageNamingCriterion(packageNamingCriterionDescriptor.getWeight());
        for (PatternDescriptor patternDescriptor : packageNamingCriterionDescriptor.getPatterns()) {
            packageNamingCriterion.addRule(Pattern.of(patternDescriptor));
        }
        return packageNamingCriterion;
    }

    public PackageNamingCriterionDescriptor materialize() {
        SARFRunner.xoManager.currentTransaction().begin();
        PackageNamingCriterionDescriptor descriptor = SARFRunner.xoManager.create(PackageNamingCriterionDescriptor.class);
        descriptor.getPatterns().addAll(
                this.rules.stream().map(Pattern::getDescriptor).collect(Collectors.toSet())
        );
        SARFRunner.xoManager.currentTransaction().commit();
        this.classificationCriterionDescriptor = descriptor;
        return descriptor;
    }
}
