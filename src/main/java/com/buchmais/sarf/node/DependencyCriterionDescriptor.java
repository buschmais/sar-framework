package com.buchmais.sarf.node;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Label("DependencyCriterion")
public interface DependencyCriterionDescriptor extends ClassificationCriterionDescriptor {

    @Relation("USES")
    @Relation.Outgoing
    Set<DependencyDescriptor> getDependencies();
}
