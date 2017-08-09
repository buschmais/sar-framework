package com.buschmais.sarf.classification.criterion;

import com.buschmais.sarf.SARFNode;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Label("ClassificationCriterion")
public interface ClassificationCriterionDescriptor extends SARFNode {

    void setWeight(double weight);

    double getWeight();

    @ClassificationCriterionCreatedDescriptor
    @Outgoing
    Set<ClassificationInfoDescriptor> getClassifications();
}
