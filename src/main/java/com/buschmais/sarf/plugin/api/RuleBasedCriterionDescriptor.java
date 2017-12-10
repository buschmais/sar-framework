package com.buschmais.sarf.plugin.api;

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
    Set<T> getRules();
}
