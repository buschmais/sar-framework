package com.buschmais.sarf.plugin.api.criterion;

import com.buschmais.sarf.plugin.api.ExecutedBy;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@ExecutedBy(RuleBasedCriterionExecutor.class)
@Label("RuleBasedClassificationCriterion")
public interface RuleBasedCriterionDescriptor<R extends RuleDescriptor> extends ClassificationCriterionDescriptor {
    @Relation("USES")
    @Relation.Outgoing
    Set<R> getRules();
}
