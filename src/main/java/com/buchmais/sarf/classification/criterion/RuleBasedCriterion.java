package com.buchmais.sarf.classification.criterion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.node.ClassificationInfoDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import com.buchmais.sarf.node.RuleBasedCriterionDescriptor;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
public abstract class RuleBasedCriterion<R extends Rule, T extends RuleBasedCriterionDescriptor> extends ClassificationCriterion<T> {

    private static final Logger LOG = LogManager.getLogger(RuleBasedCriterion.class);

    @Getter
    @Setter
    Set<R> rules;

    public RuleBasedCriterion() {
        this.rules = new TreeSet<>();
    }

    public boolean addRule(R rule) {
        return this.rules.add(rule);
    }

    @Override
    public Set<ComponentDescriptor> classify(Integer iteration) {
        LOG.info("Executing " + this.getClass().getSimpleName());
        Set<ComponentDescriptor> componentDescriptors = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });
        SARFRunner.xoManager.currentTransaction().begin();
        Set<String> matchedTypes = new TreeSet<>();
        Set<String> multipleMatchedTypes = new TreeSet<>();
        Long totalTypes = 0L;
        Long internalTypes = SARFRunner.xoManager.getRepository(TypeRepository.class).countAllInternalTypes();
        for (R r : this.rules) {
            ComponentDescriptor componentDescriptor = r.getOrCreateComponentOfCurrentIteration();
            @SuppressWarnings("unchecked")
            Set<TypeDescriptor> ts = (Set<TypeDescriptor>) r.getMatchingTypes();
            for (TypeDescriptor t : ts) {
                ClassificationInfoDescriptor info = SARFRunner.xoManager.create(ClassificationInfoDescriptor.class);
                info.setComponent(componentDescriptor);
                info.setType(t);
                info.setWeight(r.getWeight() / 100);
                info.setRule(r.getDescriptor());
                info.setIteration(iteration);
                this.getClassificationCriterionDescriptor().getClassifications().add(info);
                if (matchedTypes.contains(t.getFullQualifiedName()))
                    multipleMatchedTypes.add(t.getFullQualifiedName());
                matchedTypes.add(t.getFullQualifiedName());
                totalTypes++;
            }
            componentDescriptors.add(componentDescriptor);
        }
        SARFRunner.xoManager.currentTransaction().commit();
        LOG.info("Executed " + this.rules.size() + " Rules");
        LOG.info("\tIdentified " + componentDescriptors.size() + " Components");
        LOG.info("\tCoverage = " + (matchedTypes.size() / (double) internalTypes));
        LOG.info("\tQuality = " + (1 - multipleMatchedTypes.size() / (double) matchedTypes.size()));  // TODO: 29.06.2017 Compute per shape
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
