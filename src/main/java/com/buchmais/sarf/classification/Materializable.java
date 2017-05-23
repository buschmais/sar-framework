package com.buchmais.sarf.classification;

import com.buchmais.sarf.node.SARFNode;

/**
 * @author Stephan Pirnbaum
 */
public interface Materializable<T extends SARFNode> {
    T materialize();
}
