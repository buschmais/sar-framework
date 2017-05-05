package com.buchmais.sarf.node;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.Set;

/**
 * Node representing a component of whatever shape is needed.
 *
 * @author Stephan Pirnbaum
 */
@Label
public interface Component {

    @Outgoing
    Set<Component> getDependencies();

    @Incoming
    Set<Component> getDependentComponents();

    /**
     * Get the name of the component
     *
     * @return The name of the component
     */
    String getName();

    /**
     * Set the name of the component
     *
     * @param name The new name of the component
     */
    void setName(String name);

    /**
     * Get the shape of the component
     *
     * @return The shape of the component
     * @see Component#setShape(String)
     */
    String getShape();

    /**
     * Set the shape of the component, a shape might for example be 'Module' or 'Layer'
     *
     * @param shape The new shape of the component
     */
    void setShape(String shape);
}
