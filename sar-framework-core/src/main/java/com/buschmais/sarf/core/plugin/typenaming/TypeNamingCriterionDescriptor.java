package com.buschmais.sarf.core.plugin.typenaming;

import com.buschmais.sarf.core.plugin.api.ExecutedBy;
import com.buschmais.sarf.core.plugin.api.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@ExecutedBy(TypeNamingRuleExecutor.class)
@Label("TypeNamingCriterion")
public interface TypeNamingCriterionDescriptor extends RuleBasedCriterionDescriptor<TypeNamingRuleDescriptor> {}
