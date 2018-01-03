package com.buschmais.sarf.plugin.cohesion.evolution;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jenetics.LongGene;
import io.jenetics.Mutator;
import io.jenetics.util.MSeq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        List<Long> unusedComponentIds = new ArrayList<>();
        for (long i = 0; i < genes.get(0).getMax(); i++) {
            if (!componentToTypes.keySet().contains(i)) {
                unusedComponentIds.add(i);
            }
        }
        outer: for (Long component : componentToTypes.keySet()) {
            Multimap<Integer, Long> connectedComponents = Problem.getInstance().connectedComponents(componentToTypes.get(component));
            if (connectedComponents.keySet().size() > 1) {
                boolean first = true;
                for (Integer c : connectedComponents.keySet()) {
                    Collection<Long> types = connectedComponents.get(c);
                    if (unusedComponentIds.size() == 0) break outer;
                    for (int i = 0; i < Partitioner.ids.length; i++) {
                        if (types.contains(Partitioner.ids[i])) {
                            genes.set(i, genes.get(i).newInstance(first ? component : unusedComponentIds.get(0)));
                            mutated++;
                        }
                    }
                    if (!first) unusedComponentIds.remove(0);
                    first = false;
                }
            }
        }
        return mutated;
    }
}
