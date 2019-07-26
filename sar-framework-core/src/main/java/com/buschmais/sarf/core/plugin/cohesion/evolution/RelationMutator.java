package com.buschmais.sarf.core.plugin.cohesion.evolution;

import com.buschmais.sarf.core.plugin.cohesion.evolution.coupling.LongObjectiveCouplingChromosome;
import com.buschmais.sarf.core.plugin.cohesion.evolution.similarity.LongObjectiveSimilarityChromosome;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jenetics.Genotype;
import io.jenetics.LongGene;
import io.jenetics.Mutator;
import io.jenetics.MutatorResult;
import io.jenetics.ext.moea.Vec;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;

import java.util.Objects;
import java.util.Random;

/**
 * @author Stephan Pirnbaum
 */
public abstract class RelationMutator extends Mutator<LongGene, Vec<double[]>> {

    public RelationMutator(double probability) {
        super(probability);
    }

    @Override
    protected MutatorResult<Genotype<LongGene>> mutate(Genotype<LongGene> genotype, double p, Random random) {
        int mutated = 0;
        LongObjectiveChromosome chromosome = (LongObjectiveChromosome) genotype.getChromosome();
        MSeq<LongGene> seq = chromosome.newInstance().toSeq().asMSeq();
        Multimap<Long, Long> componentToTypes = HashMultimap.create();
        for (int i = 0; i < seq.length(); i++) {
            componentToTypes.put(seq.get(i).getAllele(), Partitioner.ids[i]);
        }
        for (int i = 0; i < seq.size(); i++) {
            Long componentId = seq.get(i).getAllele();
            long elementId = Partitioner.ids[i];
            boolean probabilityMatch = RandomRegistry.getRandom().nextDouble() < (0.008 * Math.log10(Partitioner.ids.length) / Math.log10(2));
            boolean sizeMatch = componentToTypes.get(componentId).size() == 1;
            boolean couplingMatch = Problem.getInstance().computeCouplingTo(elementId, componentToTypes.get(componentId)) == 0;
            if (probabilityMatch || sizeMatch || couplingMatch) {
                long strongestCoupledElement = Problem.getInstance().getStrongestCoupledElement(elementId);
                if (strongestCoupledElement != -1) {
                    long newComponentId = chromosome.getElementToComponent().get(strongestCoupledElement);
                    if (!Objects.equals(componentId, newComponentId)) {
                        seq.set(i, LongGene.of(newComponentId, seq.get(i).getMin(), seq.get(i).getMax()));
                        mutated++;
                    }
                }
            }
        }
        LongObjectiveChromosome newChromosome = chromosome instanceof LongObjectiveCouplingChromosome ?
            LongObjectiveCouplingChromosome.of(seq.toArray(new LongGene[seq.length()])) :
            LongObjectiveSimilarityChromosome.of(seq.toArray(new LongGene[seq.length()]));
        return MutatorResult.of(Genotype.of(newChromosome), mutated);
    }
}
