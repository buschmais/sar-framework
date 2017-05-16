package com.buchmais.sarf.node;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Label("ClassNamingCritreion")
public interface ClassNamingCriterionDescriptor extends ClassificationCriterionDescriptor {

    @Relation("USES")
    @Relation.Outgoing
    Set<PatternDescriptor> getPatterns();
}
