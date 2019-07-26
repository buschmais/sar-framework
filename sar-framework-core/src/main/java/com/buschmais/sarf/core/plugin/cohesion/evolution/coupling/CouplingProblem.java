package com.buschmais.sarf.core.plugin.cohesion.evolution.coupling;

import com.buschmais.sarf.core.plugin.cohesion.evolution.Problem;

import java.util.Collection;

/**
 * @author Stephan Pirnbaum
 */
public class CouplingProblem extends Problem {

    private CouplingProblem(int rows, int columns) {
        super(rows, columns);
    }

    @Override
    public double computeCouplingTo(long from, Collection<Long> to) {
        double coupling = 0;
        for (long id : to) {
            coupling += this.relations.get(from, id);
            coupling += this.relations.get(id, from);
        }
        return coupling;
    }

    @Override
    public double computeCohesionInComponent(Collection<Long> ids) {
        double coupling = 0;
        for (long id : ids) {
            coupling += computeCouplingTo(id, ids);
        }
        return coupling;
    }

    @Override
    public double computeCouplingBetweenComponents(Collection<Long> ids1, Collection<Long> ids2) {
        double coupling = 0;
        for (long id1 : ids1) {
            coupling += computeCouplingTo(id1, ids2);
        }
        for (long id2 : ids2) {
            coupling += computeCouplingTo(id2, ids1);
        }
        return coupling;
    }

    public static Problem newInstance(int rows, int columns) {
        return new CouplingProblem(rows, columns);
    }
}
