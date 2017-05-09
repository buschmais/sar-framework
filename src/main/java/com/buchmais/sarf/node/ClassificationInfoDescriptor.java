package com.buchmais.sarf.node;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * @author Stephan Pirnbaum
 */
@Label("ClassificationInfo")
public interface ClassificationInfoDescriptor {

    @ClassificationCriterionCreatedDescriptor
    @Incoming
    ClassificationCriterionDescriptor getClassificationCriterion();

    void setWeight(Integer weight);

    Integer getWeight();

    @Relation("CLASSIFIED")
    @Outgoing
    TypeDescriptor getType();

    void setType(TypeDescriptor typeDescriptor);

    @Relation("MAPS")
    @Outgoing
    ComponentDescriptor getComponent();

    void setComponent(ComponentDescriptor componentDescriptor);
}
