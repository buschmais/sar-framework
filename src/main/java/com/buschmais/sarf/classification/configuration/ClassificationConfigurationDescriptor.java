package com.buschmais.sarf.classification.configuration;

import com.buschmais.sarf.SARFNode;
import com.buschmais.sarf.classification.criterion.ClassificationCriterionDescriptor;
import com.buschmais.sarf.metamodel.ComponentDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Label("ClassificationConfiguration")
public interface ClassificationConfigurationDescriptor extends SARFNode {

    @Relation("CONTAINS")
    @Outgoing
    Set<ClassificationCriterionDescriptor> getClassificationCriteria();

    @Relation("DEFINES")
    @Outgoing
    Set<ComponentDescriptor> getDefinedComponents();

    void setIteration(Integer iteration);

    Integer getIteration();

    void setDecomposition(String decomposition);

    String getDecomposition();

    void setOptimization(String optimization);

    String getOptimization();
}
