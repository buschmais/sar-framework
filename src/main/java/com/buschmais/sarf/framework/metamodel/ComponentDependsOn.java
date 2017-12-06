package com.buschmais.sarf.framework.metamodel;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * @author Stephan Pirnbaum
 */
@Relation("DEPENDS_ON")
public interface ComponentDependsOn {

    @Outgoing
    ComponentDescriptor getDependentComponent();

    @Incoming
    ComponentDescriptor getDependency();

    void setWeight(int weight);
    int getWeight();
}
