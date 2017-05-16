package com.buchmais.sarf.node;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("Dependency")
public interface DependencyDescriptor extends RuleDescriptor {

    void setDependency(String dependency);

    String getDependency();
}
