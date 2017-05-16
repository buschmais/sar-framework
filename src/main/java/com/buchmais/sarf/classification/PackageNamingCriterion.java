package com.buchmais.sarf.classification;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.node.ClassificationInfoDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import com.buchmais.sarf.node.PackageNamingCriterionDescriptor;
import com.buchmais.sarf.node.PatternDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;
import java.util.TreeSet;
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

    @Override
    public Set<ComponentDescriptor> classify() {
        Set<ComponentDescriptor> componentDescriptors = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });
        SARFRunner.xoManager.currentTransaction().begin();
        for (Pattern p : this.rules) {
            ComponentDescriptor componentDescriptor = p.getOrCreateComponentOfCurrentIteration();
            for (TypeDescriptor t : p.getMatchingTypes()) {
                ClassificationInfoDescriptor info = SARFRunner.xoManager.create(ClassificationInfoDescriptor.class);
                info.setComponent(componentDescriptor);
                info.setType(t);
                info.setWeight(this.weight * p.getWeight());
                info.setRule(p.getPatternDescriptor());
                this.getClassificationCriterionDescriptor().getClassifications().add(info);
            }
            componentDescriptors.add(componentDescriptor);
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return componentDescriptors;
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
                this.rules.stream().map(Pattern::getPatternDescriptor).collect(Collectors.toSet())
        );
        SARFRunner.xoManager.currentTransaction().commit();
        this.classificationCriterionDescriptor = descriptor;
        return descriptor;
    }
}
