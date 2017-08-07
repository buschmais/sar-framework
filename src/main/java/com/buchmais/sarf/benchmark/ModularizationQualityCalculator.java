package com.buchmais.sarf.benchmark;

import com.buchmais.sarf.classification.criterion.cohesion.Problem;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public class ModularizationQualityCalculator {

    public static Double computeSimilarityBasedMQ(Map<Long, Set<Long>> decomposition) {
        Double intraConnectivity = 0d;
        Double interConnectivity = 0d;
        for (Map.Entry<Long, Set<Long>> component1 : decomposition.entrySet()) {
            long[] ids1 = component1.getValue().stream().mapToLong(l -> l).toArray();
            int denominator = ids1.length == 1 ? 1 : ((ids1.length * (ids1.length - 1)) / 2);
            intraConnectivity += Problem.getInstance().computeSimilarityCohesionInComponent(component1.getValue()) / denominator;
            for (Map.Entry<Long, Set<Long>> component2 : decomposition.entrySet()) {
                if (!Objects.equals(component1.getKey(), component2.getKey())) {
                    long[] ids2 = component2.getValue().stream().mapToLong(l -> l).toArray();
                    denominator = ((ids1.length + ids2.length) * (ids1.length + ids2.length - 1)) / 2;
                    interConnectivity += Problem.getInstance().computeSimilarityCouplingBetweenComponents(component1.getValue(), component2.getValue()) / denominator;
                }
            }
        }
        intraConnectivity /= decomposition.size();
        interConnectivity /= (decomposition.size() * (decomposition.size() - 1)) / 2;
        return intraConnectivity - interConnectivity;
    }

    public static Double computeCouplingBasedMQ(Map<Long, Set<Long>> decomposition) {
        Double intraConnectivity = 0d;
        Double interConnectivity = 0d;
        for (Map.Entry<Long, Set<Long>> component1 : decomposition.entrySet()) {
            long[] ids1 = component1.getValue().stream().mapToLong(l -> l).toArray();
            int denominator = ids1.length == 1 ? 1 : ((ids1.length * (ids1.length - 1)));
            intraConnectivity += Problem.getInstance().computeCouplingCohesionInComponent(component1.getValue()) / denominator;
            for (Map.Entry<Long, Set<Long>> component2 : decomposition.entrySet()) {
                if (!Objects.equals(component1.getKey(), component2.getKey())) {
                    long[] ids2 = component2.getValue().stream().mapToLong(l -> l).toArray();
                    denominator = ((ids1.length + ids2.length) * (ids1.length + ids2.length - 1)) / 2;
                    interConnectivity += Problem.getInstance().computeCouplingBetweenComponents(component1.getValue(), component2.getValue()) / denominator;
                }
            }
        }
        intraConnectivity /= decomposition.size();
        interConnectivity /= (decomposition.size() * (decomposition.size() - 1));
        return intraConnectivity - interConnectivity;
    }
}
