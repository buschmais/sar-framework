package com.buschmais.sarf.plugin.typenaming;

import com.buschmais.sarf.plugin.api.RuleBasedCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("TypeNamingCriterion")
public interface TypeNamingCriterionDescriptor extends RuleBasedCriterionDescriptor<TypeNamingRuleDescriptor> {}
