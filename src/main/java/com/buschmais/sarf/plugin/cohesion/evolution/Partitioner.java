package com.buschmais.sarf.plugin.cohesion.evolution;

import com.buschmais.sarf.plugin.cohesion.evolution.coupling.CouplingDrivenMutator;
import com.buschmais.sarf.plugin.cohesion.evolution.coupling.LongObjectiveCouplingChromosome;
import com.buschmais.sarf.plugin.cohesion.evolution.similarity.LongObjectiveSimilarityChromosome;
import com.buschmais.sarf.plugin.cohesion.evolution.similarity.SimilarityDrivenMutator;
import com.google.common.collect.Sets;
import io.jenetics.Chromosome;
import io.jenetics.GaussianMutator;
import io.jenetics.Genotype;
import io.jenetics.LongGene;
import io.jenetics.Phenotype;
import io.jenetics.SinglePointCrossover;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.ext.moea.MOEA;
import io.jenetics.ext.moea.NSGA2Selector;
import io.jenetics.ext.moea.UFTournamentSelector;
import io.jenetics.ext.moea.Vec;
import io.jenetics.stat.MinMax;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Slf4j
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
            .offspringFraction(0.7)
            .survivorsSelector(NSGA2Selector.ofVec())
            .offspringSelector(new UFTournamentSelector<>(Vec::dominance, Vec::compare, Vec::distance, Vec::length))
            .populationSize(populationSize)
            .alterers(
                new SinglePointCrossover<>(0.05),
                new GaussianMutator<>(0.004 * Math.log10(ids.length) / Math.log10(2)),
                similarityBased ?
                    new SimilarityDrivenMutator(0.008 * Math.log10(ids.length) / Math.log10(2)) :
                    new CouplingDrivenMutator(0.008 * Math.log10(ids.length) / Math.log10(2)),
                new SplitMutator(1))
            .executor(Runnable::run)
            .maximizing()
            .build();
        List<Genotype<LongGene>> genotypes = Arrays.asList(genotype);
        EvolutionStatistics<Vec<double[]>, MinMax<Vec<double[]>>> statistics = EvolutionStatistics.ofComparable();
        ISeq<Phenotype<LongGene, Vec<double[]>>> r = RandomRegistry.with(new Random(456), a -> engine
            .stream(genotypes)
            .limit(generations)
            .peek(statistics)
            .peek(Partitioner::update)
            .collect(MOEA.toParetoSet()));
        System.out.println(statistics);
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

    private static void update(EvolutionResult<LongGene, Vec<double[]>> evolutionResult) {
        StringBuilder updateString = new StringBuilder("Generation: ")
            .append(evolutionResult.getGeneration())
            .append("\t\tBest: ");
        evolutionResult.getPopulation().stream()
            .map(Phenotype::getFitness)
            .max(Comparator.comparingDouble(Partitioner::sumFitness))
            .ifPresent(best -> updateString
                .append("Cohesion: ")
                .append(best.data()[0])
                .append("\t\tCoupling: ")
                .append(best.data()[1]));
        LOGGER.info(updateString.toString());
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

    private static double sumFitness(Vec<double[]> vec) {
        return vec.data()[0] + vec.data()[1];// + vec.data()[2] + vec.data()[3] + vec.data()[4];
    }
}
