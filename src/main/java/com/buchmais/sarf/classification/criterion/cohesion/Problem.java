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

    public Double computeCouplingTo(Long from, long[] to) {
        return this.nodes.get(from).computeCouplingTo(to);
    }

    public Double computeCouplingCohesionInComponent(long[] ids) {
        return Arrays.stream(ids).mapToDouble(id -> computeCouplingTo(id, ids)).sum();
    }

    public Double computeCouplingBetweenComponents(long[] ids1, long[] ids2) {
        Double res = Arrays.stream(ids1).mapToDouble(id -> computeCouplingTo(id, ids2)).sum();
        res += Arrays.stream(ids2).mapToDouble(id -> computeCouplingTo(id, ids1)).sum();
        return res;
    }

    public Double computeSimilarityTo(long from, long[] to) {
        return this.nodes.get(from).computeCouplingTo(to);
    }

    public Double computeSimilarityCohesionInComponent(long[] ids) {
        return Arrays.stream(ids).mapToDouble(id -> computeSimilarityTo(id, ids)).sum();
    }

    public Double computeSimilarityCouplingBetweenComponents(long[] ids1, long[] ids2) {
        Double res = Arrays.stream(ids1).mapToDouble(id -> computeSimilarityTo(id, ids2)).sum();
        res += Arrays.stream(ids2).mapToDouble(id -> computeSimilarityTo(id, ids1)).sum();
        return res;
    }
}
