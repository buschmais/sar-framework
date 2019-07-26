package com.buschmais.sarf.core.plugin.api;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public interface Executor<E extends SARFDescriptor, T> {
    Set<T> execute(E executableDescriptor);
}
