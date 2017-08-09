package com.buschmais.sarf.classification.criterion.typenaming;

import com.buschmais.sarf.classification.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("TypeNamingCriterion")
public interface TypeNamingCriterionDescriptor extends RuleBasedCriterionDescriptor<TypeNamingRuleDescriptor> {}
