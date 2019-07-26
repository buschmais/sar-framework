package com.buschmais.sarf.core.plugin.api.criterion;

import com.buschmais.sarf.core.plugin.api.SARFDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label(value = "Rule")
public interface RuleDescriptor extends SARFDescriptor {

    void setShape(String shape);

    String getShape();

    void setName(String name);

    String getName();

    void setWeight(double weight);

    double getWeight();

    void setRule(String rule);

    String getRule();
}
