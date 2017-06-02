package com.buchmais.sarf.classification.criterion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.Dependency;
import com.buchmais.sarf.classification.Pattern;
import com.buchmais.sarf.classification.Rule;
import com.buchmais.sarf.node.*;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public abstract class RuleBasedCriterion<R extends Rule, T extends RuleBasedCriterionDescriptor> extends ClassificationCriterion<T> {

    @XmlElementWrapper(name = "Rules")
    @XmlElements(
            {
                    @XmlElement(name = "Pattern", type = Pattern.class),
                    @XmlElement(name = "Dependency", type = Dependency.class)
            }
    )
    @Setter
    Set<R> rules;

    public RuleBasedCriterion(double weight) {
        super(weight);
        this.rules = new TreeSet<>();
    }

    public boolean addRule(R rule) {
        return this.rules.add(rule);
    }

    @Override
    public Set<ComponentDescriptor> classify(Integer iteration) {
        Set<ComponentDescriptor> componentDescriptors = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });
        SARFRunner.xoManager.currentTransaction().begin();
        for (R r : this.rules) {
            ComponentDescriptor componentDescriptor = r.getOrCreateComponentOfCurrentIteration();
            @SuppressWarnings("unchecked")
            Set<TypeDescriptor> ts = (Set<TypeDescriptor>) r.getMatchingTypes();
            for (TypeDescriptor t : ts) {
                ClassificationInfoDescriptor info = SARFRunner.xoManager.create(ClassificationInfoDescriptor.class);
                info.setComponent(componentDescriptor);
                info.setType(t);
                info.setWeight(this.weight * r.getWeight() / 100);
                info.setRule(r.getDescriptor());
                info.setIteration(iteration);
                this.getClassificationCriterionDescriptor().getClassifications().add(info);
            }
            componentDescriptors.add(componentDescriptor);
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return componentDescriptors;
    }

    @Override
    public T materialize() {
        SARFRunner.xoManager.currentTransaction().begin();
        T descriptor = instantiateDescriptor();
        descriptor.getRules().addAll(
                this.rules.stream().map(R::getDescriptor).collect(Collectors.toSet())
        );
        SARFRunner.xoManager.currentTransaction().commit();
        this.classificationCriterionDescriptor = descriptor;
        return descriptor;
    }
}
