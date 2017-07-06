package com.buchmais.sarf.classification.criterion.data.node.typenaming;

import com.buchmais.sarf.classification.criterion.data.node.RuleBasedCriterionDescriptor;
import com.buchmais.sarf.node.PatternDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("ClassNamingCriterion")
public interface ClassNamingCriterionDescriptor extends RuleBasedCriterionDescriptor<PatternDescriptor> {}
