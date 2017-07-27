package com.buchmais.sarf.benchmark;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.repository.MetricRepository;

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
        MetricRepository metricRepository = SARFRunner.xoManager.getRepository(MetricRepository.class);
        for (Map.Entry<Long, Set<Long>> component1 : decomposition.entrySet()) {
            long[] ids1 = component1.getValue().stream().mapToLong(l -> l).toArray();
            int denominator = ids1.length == 1 ? 1 : ((ids1.length * (ids1.length - 1)) / 2);
            intraConnectivity += metricRepository.computeSimilarityCohesionInComponent(ids1) / denominator;
            for (Map.Entry<Long, Set<Long>> component2 : decomposition.entrySet()) {
                if (!Objects.equals(component1.getKey(), component2.getKey())) {
                    long[] ids2 = component2.getValue().stream().mapToLong(l -> l).toArray();
                    denominator = ((ids1.length + ids2.length) * (ids1.length + ids2.length - 1)) / 2;
                    interConnectivity += metricRepository.computeSimilarityCouplingBetweenComponents(ids1, ids2) / denominator;
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
        MetricRepository metricRepository = SARFRunner.xoManager.getRepository(MetricRepository.class);
        for (Map.Entry<Long, Set<Long>> component1 : decomposition.entrySet()) {
            long[] ids1 = component1.getValue().stream().mapToLong(l -> l).toArray();
            int denominator = ids1.length == 1 ? 1 : ((ids1.length * (ids1.length - 1)) / 2);
            intraConnectivity += metricRepository.computeCouplingCohesionInComponent(ids1) / denominator;
            for (Map.Entry<Long, Set<Long>> component2 : decomposition.entrySet()) {
                if (!Objects.equals(component1.getKey(), component2.getKey())) {
                    long[] ids2 = component2.getValue().stream().mapToLong(l -> l).toArray();
                    denominator = ((ids1.length + ids2.length) * (ids1.length + ids2.length - 1)) / 2;
                    interConnectivity += metricRepository.computeCouplingBetweenComponents(ids1, ids2) / denominator;
                }
            }
        }
        intraConnectivity /= decomposition.size();
        interConnectivity /= (decomposition.size() * (decomposition.size() - 1));
        return intraConnectivity - interConnectivity;
    }
}
