package com.buschmais.sarf.plugin.api;

import com.buschmais.sarf.framework.SARFDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Label("ClassificationCriterion")
public interface ClassificationCriterionDescriptor extends SARFDescriptor {

    void setWeight(double weight);

    double getWeight();

    @ClassificationCriterionCreatedDescriptor
    @Outgoing
    Set<ClassificationInfoDescriptor> getClassifications();
}
