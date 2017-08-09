package com.buschmais.sarf.classification.criterion;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Label("RuleBasedClassificationCriterion")
public interface RuleBasedCriterionDescriptor<T extends RuleDescriptor> extends ClassificationCriterionDescriptor {
    @Relation("USES")
    @Relation.Outgoing
    Set<RuleDescriptor> getRules();
}
