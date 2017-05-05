package com.buchmais.sarf.node;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Stephan Pirnbaum
 */
@Relation("DEPENDS_ON")
public interface ComponentDependsOn {

    @Outgoing
    Component getDependentComponent();

    @Incoming
    Component getDependency();

    void setWeight(int weight);
    int getWeight();
}
