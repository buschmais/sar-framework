package com.buschmais.sarf.framework.configuration;

import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.plugin.api.*;
import com.buschmais.sarf.plugin.api.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.sarf.plugin.api.criterion.RuleDescriptor;
import com.buschmais.xo.api.XOManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.el.MethodNotFoundException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
public class ClassificationConfigurationMaterializer {

    private final Logger LOG = LogManager.getLogger(ClassificationConfigurationMaterializer.class);

    private XOManager xoManager;

    @Autowired
    public ClassificationConfigurationMaterializer(XOManager xoManager) {
        this.xoManager = xoManager;
    }

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
        Set<ComponentDescriptor> componentDescriptors = mapper.definedComponents.stream()
            .map(m -> (ComponentDescriptor) genericMaterialize(m))
            .collect(Collectors.toSet());
        classificationConfigurationDescriptor.getDefinedComponents().addAll(componentDescriptors);
        // create criteria for all rules
        Map<Class<? extends RuleBasedCriterionDescriptor>, Set<RuleDescriptor>> aggregatedRules = new HashMap<>();
        for (RuleDescriptor ruleDescriptor : flattenRules(componentDescriptors)) {
            ContainedIn containedIn = ruleDescriptor.getClass().getAnnotation(ContainedIn.class);
            aggregatedRules.putIfAbsent(containedIn.value(), new HashSet<>());
            aggregatedRules.get(containedIn.value()).add(ruleDescriptor);
        }
        aggregatedRules.forEach((k, v) -> {
            RuleBasedCriterionDescriptor<RuleDescriptor> classificationCriterion = this.xoManager.create(k);
            classificationCriterion.getRules().addAll(v);
        });
        return classificationConfigurationDescriptor;
    }

    private SARFDescriptor genericMaterialize(XmlMapper mapper) {
        if (mapper.getClass().isAnnotationPresent(Materializable.class)) {
            Class<? extends SARFDescriptor> descriptorClass = mapper.getClass().getAnnotation(Materializable.class).value();
            SARFDescriptor descriptor = this.xoManager.create(descriptorClass);
            for (Field f : mapper.getClass().getDeclaredFields()) {
                if (f.isAnnotationPresent(XmlAttribute.class)) {
                    // candidate field to persist found, look for setter
                    char[] setterName = ("set" + f.getName()).toCharArray();
                    setterName[2] = Character.toUpperCase(setterName[2]);
                    try {
                        Method setter = descriptorClass.getMethod(new String(setterName), f.getType());
                        setter.setAccessible(true);
                        f.setAccessible(true);
                        setter.invoke(descriptor, f.get(mapper));
                        f.setAccessible(false);
                        setter.setAccessible(false);
                    } catch (NoSuchMethodException e) {
                        throw new MethodNotFoundException(descriptor.getClass().getName() + "has no setter for field " + f.getName() +
                            "(" + new String(setterName) + "(" + f.getType().getName() + "))");
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        // will not occur as everything necessary was checked
                        e.printStackTrace();
                    }
                } else if (f.isAnnotationPresent(XmlElement.class)) {
                    // must be materialized, can either be a collection or a single object
                    if (Collection.class.isAssignableFrom(f.getType())) {
                        char[] getterName = ("get" + f.getName()).toCharArray();
                        getterName[2] = Character.toUpperCase(getterName[2]);
                        try {
                            Method getter = descriptorClass.getDeclaredMethod(new String(getterName));
                            getter.setAccessible(true);
                            f.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            Collection<? extends XmlMapper> items = (Collection<? extends XmlMapper>) f.get(mapper);
                            @SuppressWarnings("unchecked")
                            Collection<SARFDescriptor> col = (Collection<SARFDescriptor>) getter.invoke(descriptor);
                            items.forEach(i -> col.add(genericMaterialize(i)));
                            f.setAccessible(false);
                            getter.setAccessible(false);
                        } catch (NoSuchMethodException e) {
                            throw new MethodNotFoundException(descriptor.getClass().getName() + "has no getter for field " + f.getName() +
                                "(" + new String(getterName) + ")");
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            // will not occur as everything necessary was checked
                            e.printStackTrace();
                        }
                    } else {
                        char[] setterName = ("set" + f.getName()).toCharArray();
                        setterName[2] = Character.toUpperCase(setterName[2]);
                        try {
                            Method setter = descriptorClass.getDeclaredMethod(new String(setterName));
                            setter.setAccessible(true);
                            f.setAccessible(true);
                            XmlMapper childMapper = (XmlMapper) f.get(mapper);
                            setter.invoke(descriptor, genericMaterialize(childMapper));
                            f.setAccessible(false);
                            setter.setAccessible(false);
                        } catch (NoSuchMethodException e) {
                            throw new MethodNotFoundException(descriptorClass.getName() + "has no setter for field " + f.getName() +
                                "(" + new String(setterName) + "(" + f.getType().getName() + "))");
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (ClassCastException e) {
                            throw new ClassCastException(f.getName() + " does not implement interface " + XmlMapper.class);
                        }
                    }
                }
            }
            return descriptor;
        } else {
            throw new NotMaterializableException(mapper.getClass().getName() + "is not materializable");
        }
    }

    private Set<RuleDescriptor> flattenRules(Set<ComponentDescriptor> components) {
        Set<RuleDescriptor> rules = new TreeSet<>();
        for (ComponentDescriptor component : components) {
            rules.addAll(component.getIdentifyingRules());
            rules.addAll(flattenRules(component.getContainedComponents()));
        }
        return rules;
    }
}
