package com.buschmais.sarf.plugin.api;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.framework.repository.ComponentRepository;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Component
@Lazy
public abstract class RuleExecutor<R extends RuleDescriptor> {

    protected final XOManager xoManager;

    @Autowired
    public RuleExecutor(XOManager xoManager) {
        this.xoManager = xoManager;
    }

    public abstract Set<TypeDescriptor> getMatchingTypes(R rule);

    public final ComponentDescriptor getOrCreateComponentOfCurrentIteration(R rule) {
        ComponentRepository repository = this.xoManager.getRepository(ComponentRepository.class);
        Query.Result<ComponentDescriptor> result = repository.getComponentOfCurrentIteration(rule.getShape(), rule.getName());
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
