package com.buchmais.sarf.classification.criterion.packagenaming;

import com.buchmais.sarf.classification.criterion.data.node.RuleBasedCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("PackageNamingCriterion")
public interface PackageNamingCriterionDescriptor extends RuleBasedCriterionDescriptor<PackageNamingRuleDescriptor> {}
