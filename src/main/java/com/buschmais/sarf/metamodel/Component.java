package com.buschmais.sarf.metamodel;

import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.Materializable;
import com.buschmais.sarf.classification.criterion.Rule;
import com.buschmais.sarf.classification.criterion.dependency.AnnotatedByRule;
import com.buschmais.sarf.classification.criterion.dependency.DependencyRule;
import com.buschmais.sarf.classification.criterion.dependency.ExtendsRule;
import com.buschmais.sarf.classification.criterion.dependency.ImplementsRule;
import com.buschmais.sarf.classification.criterion.packagenaming.PackageNamingRule;
import com.buschmais.sarf.classification.criterion.typenaming.TypeNamingRule;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
@EqualsAndHashCode(exclude = {"containedComponents", "identifyingRules"})
public class Component implements Comparable<Component>, Materializable<ComponentDescriptor> {

    @Getter
    @XmlID
    @XmlAttribute(name = "shape")
    private String shape;

    @Getter
    @XmlID
    @XmlAttribute(name = "name")
    private String name;

    @Getter
    @XmlElement(name = "Component")
    private Set<Component> containedComponents;

    @Getter
    @XmlElementWrapper(name = "Rules")
    @XmlElements(
            {
                    @XmlElement(name = "Name", type = TypeNamingRule.class),
                    @XmlElement(name = "Package", type = PackageNamingRule.class),
                    @XmlElement(name = "Dependency", type = DependencyRule.class),
                    @XmlElement(name = "AnnotatedBy", type = AnnotatedByRule.class),
                    @XmlElement(name = "Extends", type = ExtendsRule.class),
                    @XmlElement(name = "Implements", type = ImplementsRule.class)
            }
    )
    Set<Rule> identifyingRules;

    ComponentDescriptor descriptor;

    @Override
    public int compareTo(Component that) {
        if (this.shape.equals(that.shape)) return this.name.compareTo(that.name);
        else return this.shape.compareTo(that.shape);
    }

    @Override
    public ComponentDescriptor materialize() {
        Set<ComponentDescriptor> containedDescriptors = null;
        if (this.containedComponents != null) {
            containedDescriptors = this.containedComponents.stream().map(Component::materialize).collect(Collectors.toSet());
        }
        DatabaseHelper.xoManager.currentTransaction().begin();
        ComponentDescriptor descriptor = DatabaseHelper.xoManager.create(ComponentDescriptor.class);
        descriptor.setShape(this.shape);
        descriptor.setName(this.name);
        if (containedDescriptors != null) {
            descriptor.getContainedComponents().addAll(containedDescriptors);
        }
        DatabaseHelper.xoManager.currentTransaction().commit();
        return descriptor;
    }
}
