package com.buschmais.sarf.core.plugin.dependency;

import com.buschmais.sarf.core.plugin.api.ExecutedBy;
import com.buschmais.sarf.core.plugin.api.criterion.RuleDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@ExecutedBy(DependencyRuleExecutor.class)
@Label("Dependency")
public interface DependencyRuleDescriptor extends RuleDescriptor {}
