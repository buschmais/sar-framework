package com.buschmais.sarf.core.plugin.dependency;

import com.buschmais.sarf.core.plugin.api.ExecutedBy;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@ExecutedBy(AnnotatedByRuleExecutor.class)
@Label("AnnotatedBy")
public interface AnnotatedByRuleDescriptor extends DependencyRuleDescriptor {
}
