package com.buchmais.sarf.classification;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.node.ClassificationInfoDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import com.buchmais.sarf.node.PackageNamingCriterionDescriptor;
import com.buchmais.sarf.node.PatternDescriptor;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import org.jruby.RubyProcess;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stephan Pirnbaum
 */
public class PackageNamingCriterion extends ClassificationCriterion {

    private Set<Pattern> patterns;

    public PackageNamingCriterion() {
        this.patterns = new TreeSet<>();
    }

    public boolean addPattern(Pattern pattern) {
        return this.patterns.add(pattern);
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
        TypeRepository typeRepository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        ComponentRepository componentRepository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
        for (Pattern p : this.patterns) {
            Result<TypeDescriptor> typeDescriptorResult = typeRepository.getAllTypesInPackageLike(p.getRegEx());
            Result<ComponentDescriptor> componentDescriptorResult = componentRepository.getComponentOfCurrentIteration(p.getShape(), p.getName());
            ComponentDescriptor componentDescriptor;
            if (!componentDescriptorResult.hasResult()) {
                componentDescriptor = SARFRunner.xoManager.create(ComponentDescriptor.class);
                componentDescriptor.setShape(p.getShape());
                componentDescriptor.setName(p.getName());
            } else {
                componentDescriptor = componentDescriptorResult.getSingleResult();
            }
            for (TypeDescriptor t : typeDescriptorResult) {
                ClassificationInfoDescriptor infoDescriptor = SARFRunner.xoManager.create(ClassificationInfoDescriptor.class);
                infoDescriptor.setComponent(componentDescriptor);
                infoDescriptor.setType(t);
                infoDescriptor.setWeight(this.weight);
                this.classificationCriterionDescriptor.getClassifications().add(infoDescriptor);
                System.out.println(t.getFullQualifiedName());
            }
            componentDescriptors.add(componentDescriptor);
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return componentDescriptors;
    }

    public static PackageNamingCriterion of(PackageNamingCriterionDescriptor packageNamingCriterionDescriptor) {
        PackageNamingCriterion packageNamingCriterion = new PackageNamingCriterion();
        for (PatternDescriptor patternDescriptor : packageNamingCriterionDescriptor.getPatterns()) {
            packageNamingCriterion.addPattern(Pattern.of(patternDescriptor));
        }
        return packageNamingCriterion;
    }

    public PackageNamingCriterionDescriptor materialize() {
        SARFRunner.xoManager.currentTransaction().begin();
        PackageNamingCriterionDescriptor descriptor = SARFRunner.xoManager.create(PackageNamingCriterionDescriptor.class);
        descriptor.getPatterns().addAll(
                this.patterns.stream().map(Pattern::getPatternDescriptor).collect(Collectors.toSet())
        );
        SARFRunner.xoManager.currentTransaction().commit();
        this.classificationCriterionDescriptor = descriptor;
        return descriptor;
    }
}
