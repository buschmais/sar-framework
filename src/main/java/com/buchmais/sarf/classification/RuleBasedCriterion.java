package com.buchmais.sarf.classification;

import com.buchmais.sarf.node.ComponentDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public abstract class RuleBasedCriterion<T extends Rule> extends ClassificationCriterion {

    @XmlElementWrapper(name="Rules")
    @XmlElements(
        @XmlElement(name = "Pattern", type = Pattern.class)
    )
    @Setter
    Set<T> rules;

    public RuleBasedCriterion(double weight) {
        super(weight);
        this.rules = new TreeSet<>();
    }

    public boolean addRule(T rule) {
        return this.rules.add(rule);
    }

    @Override
    public Set<ComponentDescriptor> classify() {
        return null;
    }
}
