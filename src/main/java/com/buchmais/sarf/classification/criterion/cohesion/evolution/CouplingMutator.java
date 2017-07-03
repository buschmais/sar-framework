package com.buchmais.sarf.classification.criterion.cohesion.evolution;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.repository.MetricRepository;
import org.jenetics.LongGene;
import org.jenetics.Mutator;
import org.jenetics.util.MSeq;
import org.jenetics.util.RandomRegistry;

import java.util.Objects;
import java.util.stream.IntStream;

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
        SARFRunner.xoManager.currentTransaction().begin();
        for (int i = 0; i < genes.size(); i++) {
            if (RandomRegistry.getRandom().nextDouble() < p) {
                // mutate gene
                Long componentId = genes.get(i).getAllele();
                // compute coupling to elements in same component
                long[] typeIds = getIdsInSameComponent(componentId, genes);
                Double maxCoupling = SARFRunner.xoManager.getRepository(MetricRepository.class).computeCouplingToTypes(Partitioner.ids[i], typeIds);
                Long maxComponent = componentId;
                // the coupling to another component can be higher, find the component with the highest coupling
                for (long l : componentIds) {
                    Double coup = SARFRunner.xoManager.getRepository(MetricRepository.class).computeCouplingToTypes(Partitioner.ids[i], getIdsInSameComponent(l, genes));
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
        SARFRunner.xoManager.currentTransaction().commit();
        return mutated;
    }

    private long[] getIdsInSameComponent(Long componentId, MSeq<LongGene> genes) {
        return IntStream.range(0, genes.size())
                .filter(index -> Objects.equals(genes.get(index).getAllele(), componentId))
                .mapToLong(index -> Partitioner.ids[index])
                .toArray();
    }
}
