package com.buschmais.sarf.classification.criterion.dependency;

import com.buschmais.sarf.classification.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("DependencyCriterion")
public interface DependencyCriterionDescriptor extends RuleBasedCriterionDescriptor<DependencyDescriptor> {}
