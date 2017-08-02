package com.buchmais.sarf.classification.criterion.cohesion;

import java.util.*;

/**
 * @author Stephan Pirnbaum
 */
public class Problem {

    private Map<Long, Node> nodes;


    private static Problem instance;

    private Problem(Set<Node> nodes) {
        this.nodes = new HashMap<>();
        nodes.forEach(n -> this.nodes.put(n.getId(), n));

    }

    public static Problem getInstance() {
        return Problem.instance;
    }

    public static Problem newInstance(HashSet<Node> nodes) {
        Problem p = new Problem(nodes);
        Problem.instance = p;
        return p;
    }

    public Double computeCouplingTo(Long from, Collection<Long> to) {
        return this.nodes.get(from).computeCouplingTo(to);
    }

    public Double computeCouplingCohesionInComponent(Collection<Long> ids) {
        return ids.stream().mapToDouble(id -> computeCouplingTo(id, ids)).sum();
    }

    public Double computeCouplingBetweenComponents(Collection<Long> ids1, Collection<Long> ids2) {
        Double res = ids1.stream().mapToDouble(id -> computeCouplingTo(id, ids2)).sum();
        res += ids2.stream().mapToDouble(id -> computeCouplingTo(id, ids1)).sum();
        return res;
    }

    public Double computeSimilarityTo(long from, Collection<Long> to) {
        return this.nodes.get(from).computeSimilarityTo(to);
    }

    public Double computeSimilarityCohesionInComponent(Collection<Long> ids) {
        return ids.stream().mapToDouble(id -> computeSimilarityTo(id, ids)).sum();
    }

    public Double computeSimilarityCouplingBetweenComponents(Collection<Long> ids1, Collection<Long> ids2) {
        Double res = ids1.stream().mapToDouble(id -> computeSimilarityTo(id, ids2)).sum();
        res += ids2.stream().mapToDouble(id -> computeSimilarityTo(id, ids1)).sum();
        return res;
    }
}
