package com.buschmais.sarf.core.plugin.cohesion;

import com.buschmais.sarf.core.framework.configuration.Decomposition;
import com.buschmais.sarf.core.framework.configuration.Optimization;
import com.buschmais.sarf.core.plugin.api.ExecutedBy;
import com.buschmais.sarf.core.plugin.api.criterion.ClassificationCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@ExecutedBy(CohesionCriterionExecutor.class)
@Label("CohesionCriterion")
public interface CohesionCriterionDescriptor extends ClassificationCriterionDescriptor {

    void setDecomposition(Decomposition decomposition);

    Decomposition getDecomposition();

    void setOptimization(Optimization optimization);

    Optimization getOptimization();

    void setGenerations(Integer generations);

    Integer getGenerations();

    void setPopulationSize(Integer populationSize);

    Integer getPopulationSize();

}
