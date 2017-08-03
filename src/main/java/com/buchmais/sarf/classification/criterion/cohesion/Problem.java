package com.buchmais.sarf.classification.criterion.cohesion;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.SparseRowMatrix;

import java.util.Collection;
import java.util.Set;

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

    public Multimap<Integer, Long> connectedComponents(Collection<Long> ids) {
        Collection<Long> idCopy = Sets.newHashSet(ids);
        Multimap<Integer, Long> connectedComponents = HashMultimap.create();
        Integer compId = 0;
        while (connectedComponents.size() < ids.size()) {
            Long id = idCopy.iterator().next();
            Set<Long> identified = Sets.newHashSet(id);
            getConnectedNodes(id, idCopy, identified);
            idCopy.removeAll(identified);
            connectedComponents.putAll(compId, identified);
            compId++;
        }
        return connectedComponents;
    }

    public void getConnectedNodes(long from, Collection<Long> ids, Set<Long> identified) {
        for (long id : ids) {
            if (!identified.contains(id) && areConnected(from, id)) {
                identified.add(id);
                if (identified.size() < ids.size()) {
                    getConnectedNodes(id, ids, identified);
                } else {
                    break;
                }
            }
        }
    }

    public boolean areConnected(long id1, long id2) {
        return this.couplings.get(id1, id2) > 0 || this.couplings.get(id2, id1) > 0;
    }
}
