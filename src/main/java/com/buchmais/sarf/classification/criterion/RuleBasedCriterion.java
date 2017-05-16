package com.buchmais.sarf.classification.criterion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.Dependency;
import com.buchmais.sarf.classification.Pattern;
import com.buchmais.sarf.classification.Rule;
import com.buchmais.sarf.node.ClassificationInfoDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public abstract class RuleBasedCriterion<T extends Rule> extends ClassificationCriterion {

    @XmlElementWrapper(name = "Rules")
    @XmlElements(
            {
                    @XmlElement(name = "Pattern", type = Pattern.class),
                    @XmlElement(name = "Dependency", type = Dependency.class)
            }
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
        Set<ComponentDescriptor> componentDescriptors = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });
        SARFRunner.xoManager.currentTransaction().begin();
        for (T r : this.rules) {
            ComponentDescriptor componentDescriptor = r.getOrCreateComponentOfCurrentIteration();
            @SuppressWarnings("unchecked")
            Set<TypeDescriptor> ts = (Set<TypeDescriptor>) r.getMatchingTypes();
            for (TypeDescriptor t : ts) {
                ClassificationInfoDescriptor info = SARFRunner.xoManager.create(ClassificationInfoDescriptor.class);
                info.setComponent(componentDescriptor);
                info.setType(t);
                info.setWeight(this.weight * r.getWeight() / 100);
                info.setRule(r.getDescriptor());
                this.getClassificationCriterionDescriptor().getClassifications().add(info);
            }
            componentDescriptors.add(componentDescriptor);
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return componentDescriptors;
    }
}
