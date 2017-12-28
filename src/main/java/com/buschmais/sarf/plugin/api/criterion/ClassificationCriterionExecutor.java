package com.buschmais.sarf.plugin.api.criterion;

import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.plugin.api.Executor;

/**
 * @author Stephan Pirnbaum
 */
public interface ClassificationCriterionExecutor<E extends ClassificationCriterionDescriptor> extends Executor<E, ComponentDescriptor> {}
