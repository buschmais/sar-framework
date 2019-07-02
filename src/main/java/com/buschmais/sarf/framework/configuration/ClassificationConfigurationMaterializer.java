package com.buschmais.sarf.framework.configuration;

import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.framework.metamodel.ComponentXmlMapper;
import com.buschmais.sarf.framework.repository.AnnotationResolver;
import com.buschmais.sarf.plugin.api.*;
import com.buschmais.sarf.plugin.api.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.sarf.plugin.api.criterion.RuleDescriptor;
import com.buschmais.sarf.plugin.api.criterion.RuleXmlMapper;
import com.buschmais.xo.api.XOManager;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
@RequiredArgsConstructor
public class ClassificationConfigurationMaterializer {

    private final Logger LOG = LogManager.getLogger(ClassificationConfigurationMaterializer.class);

    private final XOManager xoManager;

    public ClassificationConfigurationDescriptor materialize(ClassificationConfigurationXmlMapper mapper) {
        ClassificationConfigurationDescriptor classificationConfigurationDescriptor =
            this.xoManager.create(ClassificationConfigurationDescriptor.class);
        // materialize all simple fields
        classificationConfigurationDescriptor.setIteration(mapper.iteration);
        classificationConfigurationDescriptor.setBasePackage(mapper.basePackage);
        classificationConfigurationDescriptor.setTypeName(mapper.typeName);
        classificationConfigurationDescriptor.setArtifact(mapper.artifact);
        classificationConfigurationDescriptor.setGenerations(mapper.generations);
        classificationConfigurationDescriptor.setPopulationSize(mapper.populationSize);
        classificationConfigurationDescriptor.setDecomposition(mapper.decomposition);
        classificationConfigurationDescriptor.setOptimization(mapper.optimization);

        // materialize components
        Set<ComponentDescriptor> componentDescriptors =
            mapper.definedComponents.stream().map(this::materializeComponent).collect(Collectors.toSet());
        classificationConfigurationDescriptor.getDefinedComponents().addAll(componentDescriptors);
        // create criteria for all rules
        Map<Class<? extends RuleBasedCriterionDescriptor<? extends RuleDescriptor>>, Set<RuleDescriptor>> aggregatedRules =
            new HashMap<>();
        for (RuleDescriptor ruleDescriptor : flattenRules(componentDescriptors)) {
            ContainedIn containedIn =
                AnnotationResolver.resolveAnnotation(ruleDescriptor.getClass(), ContainedIn.class);
            aggregatedRules.putIfAbsent(containedIn.value(), new HashSet<>());
            aggregatedRules.get(containedIn.value()).add(ruleDescriptor);
        }
        aggregatedRules.forEach((k, v) -> {
            RuleBasedCriterionDescriptor<? extends RuleDescriptor> classificationCriterion = this.xoManager.create(k);
            classificationCriterion.getRules().addAll(v);
            classificationConfigurationDescriptor.getClassificationCriteria().add(classificationCriterion);
        });
        return classificationConfigurationDescriptor;
    }

    private ComponentDescriptor materializeComponent(ComponentXmlMapper mapper) {
        ComponentDescriptor component = this.xoManager.create(ComponentDescriptor.class);
        component.setName(mapper.name);
        component.setShape(mapper.shape);

        if (mapper.containedComponents != null) {
            component.getContainedComponents().addAll(
                mapper.containedComponents.stream().map(this::materializeComponent)
                    .collect(Collectors.toList()));
        }

        if (mapper.identifyingRules != null) {
            component.getIdentifyingRules().addAll(
                mapper.identifyingRules.stream().map(rule -> this.materializeRule(rule, component))
                    .collect(Collectors.toList()));
        }

        return component;
    }

    private RuleDescriptor materializeRule(RuleXmlMapper mapper, ComponentDescriptor parent) {
        Materializable materializable =
            AnnotationResolver.resolveAnnotation(mapper.getClass(), Materializable.class);
        Class<? extends RuleDescriptor> descriptorClass =
            (Class<? extends RuleDescriptor>) materializable.value();

        RuleDescriptor rule = this.xoManager.create(descriptorClass);
        rule.setName(parent.getName());
        rule.setShape(parent.getShape());
        rule.setRule(mapper.rule);
        rule.setWeight(mapper.weight);
        return rule;
    }

    private Set<RuleDescriptor> flattenRules(Set<ComponentDescriptor> components) {
        Set<RuleDescriptor> rules = new TreeSet<>((r1, r2) -> {
            if (!r1.getShape().equals(r2.getShape())) return r1.getShape().compareTo(r2.getShape());
            if (!r1.getName().equals(r2.getName())) return r1.getName().compareTo(r2.getName());
            if (!r1.getRule().equals(r2.getRule())) return r1.getRule().compareTo(r2.getRule());
            return (int) (r1.getWeight() - r2.getWeight());
        });
        for (ComponentDescriptor component : components) {
            rules.addAll(component.getIdentifyingRules());
            rules.addAll(flattenRules(component.getContainedComponents()));
        }
        return rules;
    }
}
