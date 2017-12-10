package com.buschmais.sarf.framework;

import java.util.Collection;

/**
 * @author Stephan Pirnbaum
 */
public interface Executor<D extends SARFDescriptor, R extends Object> {
    Collection<R> execute(D descriptor);
}
