package com.buchmais.sarf.classification.criterion.cohesion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public class Problem {

    private Set<Node> nodes;


    private static Problem instance;

    private Problem(Set<Node> nodes) {
        this.nodes = Collections.unmodifiableSet(nodes);

    }

    public static Problem getInstance() {
        return Problem.instance;
    }

    public static Problem newInstance(HashSet<Node> nodes) {
        Problem p = new Problem(nodes);
        Problem.instance = p;
        return p;
    }
}
