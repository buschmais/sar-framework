package com.buchmais.sarf.classification.criterion.cohesion;

import org.jenetics.LongGene;
import org.jenetics.Mutator;
import org.jenetics.util.MSeq;
import org.jenetics.util.RandomRegistry;

import java.util.Objects;
import java.util.stream.IntStream;

/**
 * @author Stephan Pirnbaum
 */
public class SimilarityMutator extends Mutator<LongGene, Double> {

    public SimilarityMutator(double probability) {
        super(probability);
    }

    public SimilarityMutator() {
    }

    @Override
    protected int mutate(MSeq<LongGene> genes, double p) {
        int mutated = 0;
        long[] componentIds = genes.stream().mapToLong(i -> i.getAllele()).distinct().toArray();
        for (int i = 0; i < genes.size(); i++) {
            if (RandomRegistry.getRandom().nextDouble() < p) {
                // mutate gene
                Long componentId = genes.get(i).getAllele();
                // compute coupling to elements in same component
                long[] typeIds = getIdsInSameComponent(componentId, genes);
                Double maxCoupling = Problem.getInstance().computeSimilarityTo(Partitioner.ids[i], typeIds);
                Long maxComponent = componentId;
                // the coupling to another component can be higher, find the component with the highest coupling
                for (long l : componentIds) {
                    Double coup = Problem.getInstance().computeSimilarityTo(Partitioner.ids[i], getIdsInSameComponent(l, genes));
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

    private long[] getIdsInSameComponent(Long componentId, MSeq<LongGene> genes) {
        return IntStream.range(0, genes.size())
                .filter(index -> Objects.equals(genes.get(index).getAllele(), componentId))
                .mapToLong(index -> Partitioner.ids[index])
                .toArray();
    }
}
