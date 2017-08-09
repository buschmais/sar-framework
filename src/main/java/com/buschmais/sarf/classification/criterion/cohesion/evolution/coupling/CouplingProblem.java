package com.buschmais.sarf.classification.criterion.cohesion.evolution.coupling;

import com.buschmais.sarf.classification.criterion.cohesion.evolution.Problem;

import java.util.Collection;

/**
 * @author Stephan Pirnbaum
 */
public class CouplingProblem extends Problem {

    private CouplingProblem(int rows, int columns) {
        super(rows, columns);
    }

    @Override
    public Double computeCouplingTo(Long from, Collection<Long> to) {
        return to.stream().mapToDouble(id -> this.relations.get(from, id)).sum();
    }

    @Override
    public Double computeCohesionInComponent(Collection<Long> ids) {
        return ids.stream().mapToDouble(id -> computeCouplingTo(id, ids)).sum();
    }

    @Override
    public Double computeCouplingBetweenComponents(Collection<Long> ids1, Collection<Long> ids2) {
        Double res = ids1.stream().mapToDouble(id -> computeCouplingTo(id, ids2)).sum();
        res += ids2.stream().mapToDouble(id -> computeCouplingTo(id, ids1)).sum();
        return res;
    }

    public static Problem newInstance(int rows, int columns) {
        return new CouplingProblem(rows, columns);
    }
}
