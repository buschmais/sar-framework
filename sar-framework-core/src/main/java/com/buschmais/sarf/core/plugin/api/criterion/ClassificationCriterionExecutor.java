package com.buschmais.sarf.core.plugin.api.criterion;

import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.core.plugin.api.Executor;

/**
 * @author Stephan Pirnbaum
 */
public interface ClassificationCriterionExecutor<E extends ClassificationCriterionDescriptor> extends Executor<E, ComponentDescriptor> {}
