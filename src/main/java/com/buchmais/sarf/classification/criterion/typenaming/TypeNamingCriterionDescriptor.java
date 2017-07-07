package com.buchmais.sarf.classification.criterion.typenaming;

import com.buchmais.sarf.classification.criterion.data.node.RuleBasedCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("TypeNamingCriterion")
public interface TypeNamingCriterionDescriptor extends RuleBasedCriterionDescriptor<TypeNamingRuleDescriptor> {}
