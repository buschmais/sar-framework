package com.buchmais.sarf.classification.criterion.cohesion;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jenetics.LongGene;
import org.jenetics.Mutator;
import org.jenetics.util.MSeq;
import org.jenetics.util.RandomRegistry;

import java.util.Objects;

/**
 * @author Stephan Pirnbaum
 */
public class CouplingMutator extends Mutator<LongGene, Double> {

    public CouplingMutator(double probability) {
        super(probability);
    }

    public CouplingMutator() {
    }

    @Override
    protected int mutate(MSeq<LongGene> genes, double p) {
        int mutated = 0;
        long[] componentIds = genes.stream().mapToLong(i -> i.getAllele()).distinct().toArray();
        Multimap<Long, Long> componentToTypes = HashMultimap.create();
        for (int i = 0; i < genes.length(); i++) {
            componentToTypes.put(genes.get(i).getAllele(), Partitioner.ids[i]);
        }
        for (int i = 0; i < genes.size(); i++) {
            if (RandomRegistry.getRandom().nextDouble() < p) {
                // mutate gene
                Long componentId = genes.get(i).getAllele();
                // compute coupling to elements in same component
                Double maxCoupling = Problem.getInstance().computeCouplingTo(Partitioner.ids[i], componentToTypes.get(componentId));
                Long maxComponent = componentId;
                // the coupling to another component can be higher, find the component with the highest coupling
                for (long l : componentIds) {
                    Double coup = Problem.getInstance().computeCouplingTo(Partitioner.ids[i], componentToTypes.get(l));
                    if (coup > maxCoupling) {
                        maxCoupling = coup;
                        maxComponent = l;
                    }
                }
                if (!Objects.equals(maxComponent, componentId)) {
                    genes.set(i, LongGene.of(maxComponent, genes.get(i).getMin(), genes.get(i).getMax()));
                    mutated++;
                }
            }
        }
        return mutated;
    }
}
