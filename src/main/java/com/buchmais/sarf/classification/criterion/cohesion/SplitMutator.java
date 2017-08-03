package com.buchmais.sarf.classification.criterion.cohesion;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jenetics.LongGene;
import org.jenetics.Mutator;
import org.jenetics.util.MSeq;

import java.util.Collection;

/**
 * @author Stephan Pirnbaum
 */
public class SplitMutator extends Mutator<LongGene, Double> {

    public SplitMutator(double probability) {
        super(probability);
    }

    @Override
    protected int mutate(MSeq<LongGene> genes, double p) {
        int mutated = 0;
        Multimap<Long, Long> componentToTypes = HashMultimap.create();
        for (int i = 0; i < genes.length(); i++) {
            componentToTypes.put(genes.get(i).getAllele(), Partitioner.ids[i]);
        }
        Long maxComponent = componentToTypes.keySet().stream().mapToLong(l -> l).max().orElse(0);
        for (Long component : componentToTypes.keySet()) {
            Multimap<Integer, Long> connectedComponents = Problem.getInstance().connectedComponents(componentToTypes.get(component));
            if (connectedComponents.keySet().size() > 1) {
                long cmpId = component;
                for (Integer c : connectedComponents.keySet()) {
                    Collection<Long> types = connectedComponents.get(c);
                    for (int i = 0; i < Partitioner.ids.length; i++) {
                        if (types.contains(Partitioner.ids[i])) {
                            genes.set(i, LongGene.of(cmpId, genes.get(i).getMin(), genes.get(i).getMax()));
                            mutated++;
                        }
                    }
                    cmpId = maxComponent + 1;
                    maxComponent = maxComponent + 1;
                }
            }
        }
        return mutated;
    }
}
