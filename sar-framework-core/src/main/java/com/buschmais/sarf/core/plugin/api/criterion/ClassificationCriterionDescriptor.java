package com.buschmais.sarf.core.plugin.api.criterion;

import com.buschmais.sarf.core.plugin.api.ClassificationInfoDescriptor;
import com.buschmais.sarf.core.plugin.api.SARFDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
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

    @ResultOf
    @Cypher("MATCH" +
            "  (conf:ClassificationConfiguration)-[:CONTAINS]->(crit:ClassificationCriterion) " +
            "WHERE" +
            "  id(crit)={this} " +
            "RETURN" +
            "  conf.iteration")
    Integer getIteration();

    @ClassificationCriterionCreatedDescriptor
    @Outgoing
    Set<ClassificationInfoDescriptor> getClassifications();
}
