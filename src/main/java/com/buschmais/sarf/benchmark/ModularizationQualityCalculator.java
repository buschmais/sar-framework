package com.buschmais.sarf.benchmark;

import com.buschmais.sarf.classification.criterion.cohesion.evolution.Problem;
import com.buschmais.sarf.classification.criterion.cohesion.evolution.similarity.SimilarityProblem;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public class ModularizationQualityCalculator {

    public static Double computeMQ(Map<Long, Set<Long>> decomposition) {
        Double intraConnectivity = 0d;
        Double interConnectivity = 0d;
        for (Map.Entry<Long, Set<Long>> component1 : decomposition.entrySet()) {
            long[] ids1 = component1.getValue().stream().mapToLong(l -> l).toArray();
            int denominator = ids1.length == 1 ? 1 : ((ids1.length * (ids1.length - 1)) / 2);
            intraConnectivity += Problem.getInstance().computeCohesionInComponent(component1.getValue()) / denominator;
            for (Map.Entry<Long, Set<Long>> component2 : decomposition.entrySet()) {
                if (!Objects.equals(component1.getKey(), component2.getKey())) {
                    long[] ids2 = component2.getValue().stream().mapToLong(l -> l).toArray();
                    denominator = ((ids1.length + ids2.length) * (ids1.length + ids2.length - 1)) / 2;
                    interConnectivity += Problem.getInstance().computeCouplingBetweenComponents(component1.getValue(), component2.getValue()) / denominator;
                }
            }
        }
        intraConnectivity /= decomposition.size();
        interConnectivity /= (decomposition.size() * (decomposition.size() - 1)) / 2;
        if (Problem.getInstance() instanceof SimilarityProblem) {
            interConnectivity /= 2;
        }
        return intraConnectivity - interConnectivity;
    }
}
