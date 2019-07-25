package com.buschmais.sarf.core.plugin.api.criterion;

import com.buschmais.sarf.core.plugin.api.ExecutedBy;
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
    // todo use type parameter as soon this i supported by xo
    Set<RuleDescriptor> getRules();
}
