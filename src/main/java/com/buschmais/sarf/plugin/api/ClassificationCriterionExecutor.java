package com.buschmais.sarf.plugin.api;

import com.buschmais.sarf.framework.Executor;
import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;

/**
 * @author Stephan Pirnbaum
 */
public abstract class ClassificationCriterionExecutor<D extends ClassificationCriterionDescriptor> implements Executor<D, ComponentDescriptor> {}
