package com.buschmais.sarf.core.framework.configuration;

import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.core.plugin.api.SARFDescriptor;
import com.buschmais.sarf.core.plugin.api.criterion.ClassificationCriterionDescriptor;
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

    void setTypeName(String typeName);

    String getTypeName();

    void setBasePackage(String basePackage);

    String getBasePackage();

    void setArtifact(String artifact);

    String getArtifact();

}
