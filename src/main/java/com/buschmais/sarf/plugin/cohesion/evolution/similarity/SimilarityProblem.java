package com.buschmais.sarf.plugin.cohesion.evolution.similarity;

import com.buschmais.sarf.plugin.cohesion.evolution.Problem;

import java.util.Collection;

/**
 * @author Stephan Pirnbaum
 */
public class SimilarityProblem extends Problem {

    protected SimilarityProblem(int rows, int columns) {
        super(rows, columns);
    }

    @Override
    public Double computeCouplingTo(Long from, Collection<Long> to) {
        return to.stream().mapToDouble(id -> this.relations.get(from, id) + this.relations.get(id, from)).sum();
    }

    @Override
    public Double computeCohesionInComponent(Collection<Long> ids) {
        return ids.stream().mapToDouble(id -> computeCouplingTo(id, ids)).sum();
    }

    @Override
    public Double computeCouplingBetweenComponents(Collection<Long> ids1, Collection<Long> ids2) {
        return ids1.stream().mapToDouble(id -> computeCouplingTo(id, ids2)).sum();
    }

    public static Problem newInstance(int rows, int columns) {
        return new SimilarityProblem(rows, columns);
    }
}
