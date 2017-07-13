package com.buchmais.sarf.classification.criterion;

import com.buchmais.sarf.SARFNode;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label(value = "Rule")
public interface RuleDescriptor extends SARFNode {

    void setShape(String shape);

    String getShape();

    void setName(String name);

    String getName();

    void setWeight(double weight);

    double getWeight();

    void setRule(String rule);

    String getRule();
}
