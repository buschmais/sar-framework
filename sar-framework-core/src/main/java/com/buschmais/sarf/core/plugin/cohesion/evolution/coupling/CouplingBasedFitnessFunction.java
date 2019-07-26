package com.buschmais.sarf.core.plugin.cohesion.evolution.coupling;

import com.buschmais.sarf.core.plugin.cohesion.evolution.FitnessFunction;
import com.buschmais.sarf.core.plugin.cohesion.evolution.Problem;

import java.util.Set;

public class CouplingBasedFitnessFunction extends FitnessFunction {

    @Override
    protected double computeCohesion(Set<Long> elementIds) {
        int denominator = elementIds.size() == 1 ? 1 : ((elementIds.size() * (elementIds.size() - 1)) / 2);
        return Problem.getInstance().computeCohesionInComponent(elementIds) / denominator;
    }

    @Override
    protected double normalizeCoupling(double coupling, int components) {
        if (components == 1) return coupling;
        return coupling / (components * (components - 1));
    }

}
