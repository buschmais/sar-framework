package com.buchmais.sarf.node;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public interface RuleBasedCriterionDescriptor<T extends RuleDescriptor> extends ClassificationCriterionDescriptor {
    @Relation("USES")
    @Relation.Outgoing
    Set<RuleDescriptor> getRules();
}
