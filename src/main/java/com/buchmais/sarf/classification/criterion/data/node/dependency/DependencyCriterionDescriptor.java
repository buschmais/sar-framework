package com.buchmais.sarf.classification.criterion.data.node.dependency;

import com.buchmais.sarf.classification.criterion.data.node.RuleBasedCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("DependencyCriterion")
public interface DependencyCriterionDescriptor extends RuleBasedCriterionDescriptor<DependencyDescriptor> {}
