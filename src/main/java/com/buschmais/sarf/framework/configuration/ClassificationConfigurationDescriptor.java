package com.buschmais.sarf.framework.configuration;

import com.buschmais.sarf.framework.SARFDescriptor;
import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.plugin.api.ClassificationCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Label("ClassificationConfiguration")
public interface ClassificationConfigurationDescriptor extends SARFDescriptor {

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
