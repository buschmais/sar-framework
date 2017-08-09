package com.buschmais.sarf.classification.criterion.cohesion.evolution;

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
public abstract class RelationMutator extends Mutator<LongGene, Double> {

    public RelationMutator(double probability) {
        super(probability);
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
            Long componentId = genes.get(i).getAllele();
            Double maxCoupling = 0d;
            if (RandomRegistry.getRandom().nextDouble() < (0.008 * Math.log10(Partitioner.ids.length) / Math.log10(2)) || componentToTypes.get(componentId).size() == 1 ||
                    (maxCoupling = Problem.getInstance().computeCouplingTo(Partitioner.ids[i], componentToTypes.get(componentId))) == 0) {
                // mutate gene
                // compute coupling to elements in same component
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
