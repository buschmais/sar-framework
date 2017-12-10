package com.buschmais.sarf.plugin.api;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.xo.api.XOManager;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Stephan Pirnbaum
 */
public abstract class RuleBasedCriterionExecutor<C extends RuleBasedCriterionDescriptor<R>, R extends RuleDescriptor, E extends RuleExecutor<R>> {

    private static final Logger LOG = LogManager.getLogger(RuleBasedCriterionExecutor.class);

    private XOManager xoManager;

    private E ruleExecutor;

    public RuleBasedCriterionExecutor(XOManager xoManager, E ruleExecutor) {
        this.xoManager = xoManager;
        this.ruleExecutor = ruleExecutor;
    }

//    @Override todo interface
    // todo remove iteration parameter
    public Set<ComponentDescriptor> execute(C criterion, int iteration) {
        LOG.info("Executing " + this.getClass().getSimpleName());
        Set<ComponentDescriptor> componentDescriptors = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });
        this.xoManager.currentTransaction().begin();
        Set<R> rules = criterion.getRules();
        Map<String, Map<String, Set<String>>> mappedTypes = new HashMap<>();
        Long totalTypes = 0L;
        Long internalTypes = this.xoManager.getRepository(TypeRepository.class).countAllInternalTypes();
        for (R r : rules) {
            ComponentDescriptor componentDescriptor = this.ruleExecutor.getOrCreateComponentOfCurrentIteration(r);
            @SuppressWarnings("unchecked")
            Set<TypeDescriptor> ts = this.ruleExecutor.getMatchingTypes(r);
            for (TypeDescriptor t : ts) {
                ClassificationInfoDescriptor info = this.xoManager.create(ClassificationInfoDescriptor.class);
                info.setComponent(componentDescriptor);
                info.setType(t);
                info.setWeight(r.getWeight() / 100);
                info.setRule(r);
                info.setIteration(iteration);
                criterion.getClassifications().add(info);
                if (mappedTypes.containsKey(t.getFullQualifiedName())) {
                    mappedTypes.get(t.getFullQualifiedName()).merge(
                        componentDescriptor.getShape(),
                        Sets.newHashSet(componentDescriptor.getName()),
                        (s1, s2) -> {
                            s1.addAll(s2);
                            return s1;
                        }
                    );
                } else {
                    Map<String, Set<String>> start = new HashMap<>();
                    start.put(componentDescriptor.getShape(), Sets.newHashSet(componentDescriptor.getName()));
                    mappedTypes.put(t.getFullQualifiedName(), start);
                }
                totalTypes++;
            }
            componentDescriptors.add(componentDescriptor);
        }
        Long multipleMatched = 0L;
        for (Map.Entry<String, Map<String, Set<String>>> mappings : mappedTypes.entrySet()) {
            for (Map.Entry<String, Set<String>> components : mappings.getValue().entrySet()) {
                if (components.getValue().size() > 1) {
                    multipleMatched++;
                    break;
                }
            }
        }
        this.xoManager.currentTransaction().commit();
        LOG.info("Executed " + rules.size() + " Rules");
        LOG.info("\tIdentified " + componentDescriptors.size() + " Components");
        LOG.info("\tCoverage = " + (mappedTypes.size() / (double) internalTypes));
        LOG.info("\tQuality = " + (1 - multipleMatched / (double) mappedTypes.size()));
        return componentDescriptors;
    }
}
