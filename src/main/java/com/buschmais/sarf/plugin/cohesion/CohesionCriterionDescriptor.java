package com.buschmais.sarf.plugin.cohesion;

import com.buschmais.sarf.plugin.api.ExecutedBy;
import com.buschmais.sarf.plugin.api.criterion.ClassificationCriterionDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@ExecutedBy(CohesionCriterionExecutor.class)
@Label("CohesionCriterion")
public interface CohesionCriterionDescriptor extends ClassificationCriterionDescriptor {

}
