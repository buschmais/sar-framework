package com.buschmais.sarf.core.plugin.packagenaming;

import com.buschmais.sarf.core.plugin.api.ExecutedBy;
import com.buschmais.sarf.core.plugin.api.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@ExecutedBy(PackageNamingRuleExecutor.class)
@Label("PackageNamingCriterion")
public interface PackageNamingCriterionDescriptor extends RuleBasedCriterionDescriptor<PackageNamingRuleDescriptor> {}
