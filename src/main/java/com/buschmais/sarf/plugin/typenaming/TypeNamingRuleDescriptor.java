package com.buschmais.sarf.plugin.typenaming;

import com.buschmais.sarf.plugin.api.ExecutedBy;
import com.buschmais.sarf.plugin.api.criterion.RuleDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@ExecutedBy(TypeNamingRuleExecutor.class)
@Label("TypeNamingRule")
public interface TypeNamingRuleDescriptor extends RuleDescriptor {
}
