package com.buschmais.sarf.framework.configuration;

import com.buschmais.sarf.framework.Materializable;
import com.buschmais.sarf.framework.SARFDescriptor;
import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.plugin.api.RuleBasedCriterionDescriptor;
import com.buschmais.sarf.plugin.api.RuleXmlMapper;
import com.buschmais.xo.api.XOManager;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.el.MethodNotFoundException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
        this.xoManager.currentTransaction().begin();
        ClassificationConfigurationDescriptor classificationConfigurationDescriptor =
            this.xoManager.create(ClassificationConfigurationDescriptor.class);
        for (Field f : mapper.getClass().getDeclaredFields()) {

        }
        this.xoManager.currentTransaction().commit();
        return classificationConfigurationDescriptor;
    }

    private void genericMaterialize(ClassificationConfigurationXmlMapper mapper) {
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
                            Collection<? extends SARFDescriptor> items = (Collection<? extends SARFDescriptor>) getter.invoke(descriptor);
                            items.stream().map(d -> )
                            items.addAll(((Collection) f.get(mapper)).stream;);
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

                    }
                }
            }

        } else {
            throw new NotMaterializableException(mapper.getClass().getName() + "is not materializable");
        }
    }

    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        Set<RuleXmlMapper> rules = flatten(this.model);
        Map<Class<? extends RuleBasedCriterionDescriptor>, RuleBasedCriterionDescriptor> criteriaMapping = new HashMap<>();
        for (RuleXmlMapper<?> r : rules) {
            Materializable materializable = r.getClass().getAnnotation(Materializable.class);
            try {
                criteriaMapping.merge(r.getAssociateCriterion(), r.getAssociateCriterion().newInstance().addRule(r), (c1, c2) -> {
                    c1.addRule(r);
                    return c1;
                });
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        this.classificationCriteria = Sets.newTreeSet(criteriaMapping.values());
    }

    private Set<RuleXmlMapper> flatten(Set<ComponentDescriptor> components) {
        Set<RuleXmlMapper> rules = new TreeSet<>();
        for (ComponentDescriptor component : components) {
            if (component.getIdentifyingRules() != null) {
                rules.addAll(component.getIdentifyingRules());
            }
            if (component.getContainedComponents() != null) {
                rules.addAll(flatten(component.getContainedComponents()));
            }
        }
        return rules;
    }
}
