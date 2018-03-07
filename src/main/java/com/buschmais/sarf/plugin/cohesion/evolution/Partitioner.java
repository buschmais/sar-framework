package com.buschmais.sarf.plugin.cohesion.evolution;

import com.buschmais.sarf.plugin.cohesion.evolution.coupling.CouplingDrivenMutator;
import com.buschmais.sarf.plugin.cohesion.evolution.coupling.LongObjectiveCouplingChromosome;
import com.buschmais.sarf.plugin.cohesion.evolution.similarity.LongObjectiveSimilarityChromosome;
import com.buschmais.sarf.plugin.cohesion.evolution.similarity.SimilarityDrivenMutator;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.ext.moea.MOEA;
import io.jenetics.ext.moea.NSGA2Selector;
import io.jenetics.ext.moea.Vec;
import io.jenetics.util.ISeq;

import java.util.*;
import java.util.concurrent.Executors;

/**
 * @author Stephan Pirnbaum
 */
public class Partitioner {

    static long[] ids;

    static int generations;

    static long lastGeneration = 0;

    public static Map<Long, Set<Long>> partition(long[] ids, Map<Long, Set<Long>> initialPartitioning, int generations, int populationSize, boolean similarityBased) {
        Partitioner.ids = ids;
        Partitioner.generations = generations;
        Genotype<LongGene> genotype = createGenotype(initialPartitioning, similarityBased);
        final Engine<LongGene, Vec<double[]>> engine = Engine
                .builder(i -> {
                    LongObjectiveChromosome chromosome = (LongObjectiveChromosome) i.getChromosome();
                    return Vec.of(
                        chromosome.getCohesionObjective(),
                        chromosome.getCouplingObjective()/*,
                        chromosome.getCohesiveComponentObjective(),
                        chromosome.getComponentRangeObjective(),
                        chromosome.getComponentSizeObjective()*/
                    );
                }, genotype)
                .survivorsSelector(NSGA2Selector.ofVec())
                .offspringSelector(new TournamentSelector<>(3))
                .populationSize(populationSize)
                .alterers(
                        new SinglePointCrossover<>(1),
                        new GaussianMutator<>(0.004 * Math.log10(ids.length) / Math.log10(2)),
                        similarityBased ?
                                new SimilarityDrivenMutator(0.008 * Math.log10(ids.length) / Math.log10(2)) :
                                new CouplingDrivenMutator(1),
                        new SplitMutator(1))
                .executor(Executors.newCachedThreadPool())
                .maximizing()
                .build();
        List<Genotype<LongGene>> genotypes = Arrays.asList(genotype);
        ISeq<Phenotype<LongGene, Vec<double[]>>> r = engine
                .stream(genotypes)
                .limit(generations)
                .peek(Partitioner::update)
                .collect(MOEA.toParetoSet());
        Phenotype<LongGene, Vec<double[]>> best = r.stream()
            .max(Comparator.comparingDouble(p -> sumFitness(p.getFitness()))).orElse(null);
        Map<Long, Set<Long>> identifiedComponents = new HashMap<>();
        assert best != null;
        for (int i = 0; i < best.getGenotype().getChromosome().length(); i++) {
            identifiedComponents.merge(
                    best.getGenotype().getChromosome().getGene(i).getAllele(),
                    Sets.newHashSet(ids[i]),
                    (s1, s2) -> {
                        s1.addAll(s2);
                        return s1;
                    });
        }
        return identifiedComponents;

    }

    private static Genotype<LongGene> createGenotype(Map<Long, Set<Long>> initialPartitioning, boolean similarityBased) {
        List<LongGene> genes = new LinkedList<>();
        for (Long id : ids) {
            int compId = 0;
            boolean found = false;
            for (Map.Entry<Long, Set<Long>> entry : initialPartitioning.entrySet()) {
                if (entry.getValue().contains(id)) {
                    genes.add(LongGene.of(compId, 0, ids.length / 2 - 1));
                    found = true;
                }
                compId++;
            }
            if (!found) {
                genes.add(LongGene.of(compId, 0, ids.length / 2 - 1));
            }
        }
        Chromosome<LongGene> chromosome =
                similarityBased ?
                        LongObjectiveSimilarityChromosome.of(genes.toArray(new LongGene[genes.size()])) :
                        LongObjectiveCouplingChromosome.of(genes.toArray(new LongGene[genes.size()]));
        return Genotype.of(chromosome);
    }

    private static void update(final EvolutionResult<LongGene, Vec<double[]>> result) {
        int percentage = (int) (1.0 * result.getGeneration() / generations * 100);
        int left = 100 - percentage;
        System.out.print("\rProgress: " + Strings.repeat("=", percentage) + Strings.repeat(" ", left) + "| " + result.getPopulation().size());
    }

    private static double sumFitness(Vec<double[]> vec) {
        return vec.data()[0] + vec.data()[1];// + vec.data()[2] + vec.data()[3] + vec.data()[4];
    }
}
