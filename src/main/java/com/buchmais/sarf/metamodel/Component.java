package com.buchmais.sarf.metamodel;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.Materializable;
import com.buchmais.sarf.classification.criterion.logic.Rule;
import com.buchmais.sarf.classification.criterion.logic.dependency.DependencyRule;
import com.buchmais.sarf.classification.criterion.logic.packagenaming.PackageNamingRule;
import com.buchmais.sarf.classification.criterion.typenaming.TypeNamingRule;
import com.buchmais.sarf.node.ComponentDescriptor;
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
                    @XmlElement(name = "Dependency", type = DependencyRule.class)
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
        SARFRunner.xoManager.currentTransaction().begin();
        ComponentDescriptor descriptor = SARFRunner.xoManager.create(ComponentDescriptor.class);
        descriptor.setShape(this.shape);
        descriptor.setName(this.name);
        if (containedDescriptors != null) {
            descriptor.getContainedComponents().addAll(containedDescriptors);
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return descriptor;
    }
}
