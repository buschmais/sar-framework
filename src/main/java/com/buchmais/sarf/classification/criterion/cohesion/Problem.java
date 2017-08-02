package com.buchmais.sarf.classification.criterion.cohesion;

import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.SparseRowMatrix;

import java.util.Collection;

/**
 * @author Stephan Pirnbaum
 */
public class Problem {

    private AMatrix couplings;

    private AMatrix similarities;

    private static Problem instance;

    private Problem(int rows, int columns) {
        this.couplings = SparseRowMatrix.create(rows, columns);
        this.similarities = SparseRowMatrix.create(rows, columns);
    }

    public static Problem getInstance() {
        return Problem.instance;
    }

    public static Problem newInstance(int rows, int columns) {
        Problem p = new Problem(rows, columns);
        Problem.instance = p;
        return p;
    }

    public void addCoupling(int from, int to, double coupling) {
        this.couplings.set(from, to, coupling);
    }

    public void addSimilarity(int from, int to, double similarity) {
        this.similarities.set(from, to, similarity);
    }

    public Double computeCouplingTo(Long from, Collection<Long> to) {
        return to.stream().mapToDouble(id -> this.couplings.get(from, id)).sum();
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
        return to.stream().mapToDouble(id -> this.similarities.get(from, id)).sum();
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
