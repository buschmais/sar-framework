package com.buschmais.sarf;

/**
 * @author Stephan Pirnbaum
 */
public interface Materializable<T extends SARFNode> {
    T materialize();
}
