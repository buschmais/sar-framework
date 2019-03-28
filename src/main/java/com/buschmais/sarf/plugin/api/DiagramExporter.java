package com.buschmais.sarf.plugin.api;

import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;

import java.util.Set;

/**
 * Exports component data for visualizing it with a certain type of diagram.
 */
public interface DiagramExporter {

    /**
     * Export the component data.
     *
     * @param components all components of the solution
     * @return a text-based representation of the components which serve as input for the diagram.
     */
    String export(Set<ComponentDescriptor> components);

}
