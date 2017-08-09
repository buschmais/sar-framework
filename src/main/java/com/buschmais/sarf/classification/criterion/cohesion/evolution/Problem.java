package com.buschmais.sarf.classification.criterion.cohesion.evolution;

import com.buschmais.sarf.classification.criterion.cohesion.evolution.coupling.CouplingProblem;
import com.buschmais.sarf.classification.criterion.cohesion.evolution.similarity.SimilarityProblem;
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
public abstract class Problem {

    protected AMatrix relations;

    private static Problem instance;

    protected Problem(int rows, int columns) {
        this.relations = SparseRowMatrix.create(rows, columns);
    }

    public static Problem getInstance() {
        return Problem.instance;
    }

    public static Problem newInstance(int rows, int columns, boolean similarityBased) {
        Problem.instance =
                similarityBased ?
                        SimilarityProblem.newInstance(rows, columns) :
                        CouplingProblem.newInstance(rows, columns);
        return Problem.instance;
    }

    public void addRelation(int from, int to, double coupling) {
        this.relations.set(from, to, coupling);
    }

    public abstract Double computeCouplingTo(Long from, Collection<Long> to);

    public abstract Double computeCohesionInComponent(Collection<Long> ids);

    public abstract Double computeCouplingBetweenComponents(Collection<Long> ids1, Collection<Long> ids2);

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

    public boolean isFullyConnected(Collection<Long> ids) {
        Set<Long> connectedNodes = Sets.newHashSet(ids.iterator().next());
        getConnectedNodes(ids.iterator().next(), ids, connectedNodes);
        return connectedNodes.size() == ids.size();
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
        return this.relations.get(id1, id2) > 0 || this.relations.get(id2, id1) > 0;
    }
}
