package com.buschmais.sarf.plugin.api;

import com.buschmais.sarf.framework.metamodel.ComponentXmlMapper;
import lombok.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor
@EqualsAndHashCode
public abstract class RuleXmlMapper<T extends RuleDescriptor> implements Comparable<RuleXmlMapper> {

    @Getter
    @Setter
    String shape;

    @Getter
    @Setter
    String name;

    @Getter
    @XmlAttribute(name = "weight")
    double weight;

    @Getter
    @XmlAttribute(name = "rule")
    protected String rule;

    @Override
    public int compareTo(RuleXmlMapper o) {
        if (!shape.equals(o.getShape())) return shape.compareTo(o.getShape());
        if (!name.equals(o.getName())) return name.compareTo(o.getName());
        if (!rule.equals(o.getRule())) return rule.compareTo(o.getRule());
        return (int) (weight - o.getWeight());
    }

    @SuppressWarnings("unused")
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        this.shape = ((ComponentXmlMapper) parent).getShape();
        this.name = ((ComponentXmlMapper) parent).getName();
    }
}
