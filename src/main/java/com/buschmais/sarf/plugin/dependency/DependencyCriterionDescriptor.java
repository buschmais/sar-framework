package com.buschmais.sarf.plugin.dependency;

import com.buschmais.sarf.plugin.api.RuleBasedCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("DependencyCriterion")
public interface DependencyCriterionDescriptor extends RuleBasedCriterionDescriptor<DependencyDescriptor> {}
