package com.buschmais.sarf.core.plugin.packagenaming;

import com.buschmais.sarf.core.plugin.api.ContainedIn;
import com.buschmais.sarf.core.plugin.api.ExecutedBy;
import com.buschmais.sarf.core.plugin.api.criterion.RuleDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@ExecutedBy(PackageNamingRuleExecutor.class)
@ContainedIn(PackageNamingCriterionDescriptor.class)
@Label("PackageNamingRule")
public interface PackageNamingRuleDescriptor extends RuleDescriptor {}
