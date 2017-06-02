package com.buchmais.sarf.node;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Label("ClassNamingCriterion")
public interface ClassNamingCriterionDescriptor extends RuleBasedCriterionDescriptor<PatternDescriptor> {}
