package com.buschmais.sarf.plugin.cohesion.evolution;

import com.buschmais.sarf.plugin.cohesion.evolution.coupling.LongObjectiveCouplingChromosome;
import com.buschmais.sarf.plugin.cohesion.evolution.similarity.LongObjectiveSimilarityChromosome;
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
        long[] componentIds = seq.stream().mapToLong(i -> i.getAllele()).distinct().toArray();
        Multimap<Long, Long> componentToTypes = HashMultimap.create();
        for (int i = 0; i < seq.length(); i++) {
            componentToTypes.put(seq.get(i).getAllele(), Partitioner.ids[i]);
        }
        for (int i = 0; i < seq.size(); i++) {
            Long componentId = seq.get(i).getAllele();
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
                    seq.set(i, LongGene.of(maxComponent, seq.get(i).getMin(), seq.get(i).getMax()));
                    mutated++;
                }
            }
        }
        LongObjectiveChromosome newChromosome = chromosome instanceof LongObjectiveCouplingChromosome ?
            LongObjectiveCouplingChromosome.of(seq.toArray(new LongGene[seq.length()])) :
            LongObjectiveSimilarityChromosome.of(seq.toArray(new LongGene[seq.length()]));
        return MutatorResult.of(Genotype.of(newChromosome), mutated);
    }
}
