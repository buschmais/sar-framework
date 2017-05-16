package com.buchmais.sarf.classification;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.node.ComponentDescriptor;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import lombok.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
@EqualsAndHashCode
public abstract class Rule implements Comparable<Rule> {

    @Getter
    @XmlAttribute(name = "shape")
    String shape;

    @Getter
    @XmlAttribute(name = "name")
    String name;

    @Getter
    @XmlAttribute(name = "weight")
    double weight;

    public ComponentDescriptor getOrCreateComponentOfCurrentIteration() {
        ComponentRepository repository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
        Result<ComponentDescriptor> result = repository.getComponentOfCurrentIteration(this.shape, this.name);
        ComponentDescriptor componentDescriptor;
        if (result.hasResult()) {
            componentDescriptor = result.getSingleResult();
        } else {
            componentDescriptor = SARFRunner.xoManager.create(ComponentDescriptor.class);
            componentDescriptor.setShape(this.shape);
            componentDescriptor.setName(this.name);
        }
        return componentDescriptor;
    }

    public abstract Set<TypeDescriptor> getMatchingTypes();

    public int compareTo(Rule o) {
        if (!shape.equals(o.getShape())) return shape.compareTo(o.getShape());
        if (!name.equals(o.getName())) return name.compareTo(o.getName());
        return (int) (weight - o.getWeight());
    }
}
