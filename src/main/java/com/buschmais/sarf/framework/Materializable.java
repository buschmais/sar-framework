package com.buschmais.sarf.framework;

/**
 * @author Stephan Pirnbaum
 */
public interface Materializable<T extends SARFDescriptor> {
    T materialize();
}
