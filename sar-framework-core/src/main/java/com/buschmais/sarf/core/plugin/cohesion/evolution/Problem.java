package com.buschmais.sarf.core.plugin.cohesion.evolution;

import com.buschmais.sarf.core.plugin.cohesion.ElementCoupling;
import com.buschmais.sarf.core.plugin.cohesion.evolution.coupling.CouplingProblem;
import com.buschmais.sarf.core.plugin.cohesion.evolution.similarity.SimilarityProblem;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.SparseRowMatrix;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public abstract class Problem {

    protected AMatrix relations;

    protected Map<ElementCoupling, ElementCoupling> couplings;

    /**
     * Mapping from an element which is referenced by its id as in {@link Partitioner#ids} to the element to which it is
     * coupled the most.
     */
    private Map<Long, ElementCoupling> highestCoupling = new HashMap<>();

    private static Problem instance;

    protected Problem(int rows, int columns) {
        this.relations = SparseRowMatrix.create(rows, columns);
        couplings = new HashMap<>();
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

    public void addRelation(long from, long to, double coupling) {
        this.relations.set((int) from, (int) to, coupling);
        ElementCoupling eC = new ElementCoupling(from, coupling, to);
        // todo multiple best coupled elements?
        // todo bi-directionality?
        if (this.highestCoupling.containsKey(from)) {
            if (this.highestCoupling.get(from).getCoupling() < coupling) {
                this.highestCoupling.put(from, eC);
            }
        } else {
            this.highestCoupling.put(from, eC);
        }
        this.couplings.put(eC, eC);
    }

    public abstract Double computeCouplingTo(long from, Collection<Long> to);

    public abstract Double computeCohesionInComponent(Collection<Long> ids);

    public abstract Double computeCouplingBetweenComponents(Collection<Long> ids1, Collection<Long> ids2);

    public Multimap<Long, Long> connectedComponents(Collection<Long> ids) {
        Collection<Long> idCopy = Sets.newHashSet(ids);
        Multimap<Long, Long> connectedComponents = HashMultimap.create();
        long compId = 0;
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
        return this.relations.get(id1, id2) > 0 || this.relations.get(id2, id1) > 0;
    }

    public long getStrongestCoupledElement(long forElement) {
        return this.highestCoupling.get(forElement) != null ? this.highestCoupling.get(forElement).getTarget() : -1;
    }
}
