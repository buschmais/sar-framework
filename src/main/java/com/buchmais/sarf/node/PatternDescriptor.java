package com.buchmais.sarf.node;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Stephan Pirnbaum
 */
@Label("Pattern")
public interface PatternDescriptor extends RuleDescriptor {

    void setShape(String shape);

    String getShape();

    void setName(String name);

    String getName();

    void setRegEx(String regEx);

    String getRegEx();

    void setWeight(double weight);

    double getWeight();
}
