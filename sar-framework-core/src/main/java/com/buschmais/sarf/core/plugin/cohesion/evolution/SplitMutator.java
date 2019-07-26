package com.buschmais.sarf.core.plugin.cohesion.evolution;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jenetics.Genotype;
import io.jenetics.LongGene;
import io.jenetics.Mutator;
import io.jenetics.MutatorResult;
import io.jenetics.ext.moea.Vec;
import io.jenetics.util.MSeq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * @author Stephan Pirnbaum
 */
public class SplitMutator extends Mutator<LongGene, Vec<double[]>> {

    public SplitMutator(double probability) {
        super(probability);
    }

    @Override
    protected MutatorResult<Genotype<LongGene>> mutate(Genotype<LongGene> genotype, double p, Random random) {
        int mutated = 0;
        LongObjectiveChromosome chromosome = (LongObjectiveChromosome) genotype.getChromosome();
        MSeq<LongGene> seq = chromosome.toSeq().asMSeq();
        Multimap<Long, Long> componentToTypes = HashMultimap.create();
        for (int i = 0; i < seq.length(); i++) {
            componentToTypes.put(seq.get(i).getAllele(), Partitioner.ids[i]);
        }
        List<Long> unusedComponentIds = new ArrayList<>();
        for (long i = 0; i < seq.get(0).getMax(); i++) {
            if (!componentToTypes.keySet().contains(i)) {
                unusedComponentIds.add(i);
            }
        }
        outer: for (Long component : componentToTypes.keySet()) {
            Multimap<Long, Long> connectedComponents = Problem.getInstance().connectedComponents(componentToTypes.get(component));
            if (connectedComponents.keySet().size() > 1) {
                boolean first = true;
                for (long c : connectedComponents.keySet()) {
                    Collection<Long> types = connectedComponents.get(c);
                    if (unusedComponentIds.size() == 0) break outer;
                    for (int i = 0; i < Partitioner.ids.length; i++) {
                        if (types.contains(Partitioner.ids[i])) {
                            seq.set(i, seq.get(i).newInstance(first ? component : unusedComponentIds.get(0)));
                            mutated++;
                        }
                    }
                    if (!first) unusedComponentIds.remove(0);
                    first = false;
                }
            }
        }
        LongObjectiveChromosome newChromosome = new LongObjectiveChromosome(seq.asISeq());

        return MutatorResult.of(Genotype.of(newChromosome), mutated);
    }
}
