package com.buschmais.sarf.core.plugin.api.criterion;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.core.framework.repository.AnnotationResolver;
import com.buschmais.sarf.core.framework.repository.ComponentRepository;
import com.buschmais.sarf.core.framework.repository.TypeRepository;
import com.buschmais.sarf.core.plugin.api.ClassificationInfoDescriptor;
import com.buschmais.sarf.core.plugin.api.ExecutedBy;
import com.buschmais.sarf.core.plugin.api.Executor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.XOManager;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
@RequiredArgsConstructor
@Slf4j
public class RuleBasedCriterionExecutor<C extends RuleBasedCriterionDescriptor> implements Executor<C, ComponentDescriptor> {

    private final XOManager xoManager;
    private final BeanFactory beanFactory;
    private final TypeRepository typeRepository;
    private final ComponentRepository componentRepository;

    @Override
    public Set<ComponentDescriptor> execute(C executableDescriptor) {
        LOGGER.info("Executing " + this.getClass().getSimpleName());
        Set<ComponentDescriptor> componentDescriptors = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });
//        this.xoManager.currentTransaction().begin();
        Set<RuleDescriptor> rules = executableDescriptor.getRules();
        Map<String, Map<String, Set<String>>> mappedTypes = new HashMap<>();
        Long internalTypes = this.typeRepository.countAllInternalTypes();
        for (RuleDescriptor r : rules) {
            ExecutedBy executedBy = AnnotationResolver.resolveAnnotation(r.getClass(), ExecutedBy.class);
            RuleExecutor<RuleDescriptor> ruleExecutor = (RuleExecutor<RuleDescriptor>) this.beanFactory.getBean(executedBy.value());
            ComponentDescriptor componentDescriptor = getOrCreateComponentOfCurrentIteration(r);
            Set<TypeDescriptor> ts = ruleExecutor.execute(r);
            for (TypeDescriptor t : ts) {
                ClassificationInfoDescriptor info = this.xoManager.create(ClassificationInfoDescriptor.class);
                info.setComponent(componentDescriptor);
                info.setType(t);
                info.setWeight(r.getWeight() / 100);
                info.setRule(r);
                info.setIteration(executableDescriptor.getIteration());
                executableDescriptor.getClassifications().add(info);
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
 //       this.xoManager.currentTransaction().commit();
        LOGGER.info("Executed " + rules.size() + " Rules");
        LOGGER.info("\tIdentified " + componentDescriptors.size() + " Components");
        LOGGER.info("\tCoverage = " + (mappedTypes.size() / (double) internalTypes));
        LOGGER.info("\tQuality = " + (1 - multipleMatched / (double) mappedTypes.size()));
        return componentDescriptors;
    }

    private ComponentDescriptor getOrCreateComponentOfCurrentIteration(RuleDescriptor rule) {
        Result<ComponentDescriptor> result = this.componentRepository.getComponentOfCurrentIteration(rule.getShape(), rule.getName());
        ComponentDescriptor componentDescriptor;
        if (result.hasResult()) {
            componentDescriptor = result.getSingleResult();
        } else {
            componentDescriptor = this.xoManager.create(ComponentDescriptor.class);
            componentDescriptor.setShape(rule.getShape());
            componentDescriptor.setName(rule.getName());
        }
        return componentDescriptor;
    }
}
