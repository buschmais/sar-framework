package com.buchmais.sarf.classification.criterion.data.node.packagenaming;

import com.buchmais.sarf.classification.criterion.data.node.RuleBasedCriterionDescriptor;
import com.buchmais.sarf.node.PatternDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("PackageNamingCriterion")
public interface PackageNamingCriterionDescriptor extends RuleBasedCriterionDescriptor<PatternDescriptor> {}
