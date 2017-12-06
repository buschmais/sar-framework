package com.buschmais.sarf.plugin.cohesion.evolution;

import com.buschmais.sarf.plugin.cohesion.evolution.coupling.CouplingDrivenMutator;
import com.buschmais.sarf.plugin.cohesion.evolution.coupling.LongObjectiveCouplingChromosome;
import com.buschmais.sarf.plugin.cohesion.evolution.similarity.LongObjectiveSimilarityChromosome;
import com.buschmais.sarf.plugin.cohesion.evolution.similarity.SimilarityDrivenMutator;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
public class Partitioner {

    static Genotype<LongGene> best;
    static Double bestFitness;

    static long[] ids;

    static int generations;

    static long lastGeneration = 0;

    private static long bestGeneration;

    public static Map<Long, Set<Long>> partition(long[] ids, Map<Long, Set<Long>> initialPartitioning, int generations, int populationSize, boolean similarityBased) {
        Partitioner.best = null;
        Partitioner.bestFitness = Double.MIN_VALUE;
        Partitioner.ids = ids;
        Partitioner.generations = generations;
        Genotype<LongGene> genotype = createGenotype(initialPartitioning, similarityBased);
        final Engine<LongGene, Double> engine = Engine
                .builder(Partitioner::computeFitnessValue, genotype)
                .offspringFraction(0.5)
                .survivorsSelector(new ParetoFrontierSelector())
                .offspringSelector(new ParetoFrontierSelector())
                .populationSize(populationSize)
                .alterers(
                        new SinglePointCrossover<>(1),
                        new GaussianMutator<>(0.004 * Math.log10(ids.length) / Math.log10(2)),
                        similarityBased ?
                                new SimilarityDrivenMutator(0.008 * Math.log10(ids.length) / Math.log10(2)) :
                                new CouplingDrivenMutator(1),
                        new SplitMutator(1))
                .executor(Executors.newCachedThreadPool())
                .build();
        List<Genotype<LongGene>> genotypes = Arrays.asList(genotype);
        List<EvolutionResult> r = engine
                .stream(genotypes)
                //.limit(limit.byFitnessThreshold(1d))
                .limit(generations)
                .peek(Partitioner::update)
                .collect(Collectors.toList());
        Map<Long, Set<Long>> identifiedComponents = new HashMap<>();
        for (int i = 0; i < best.getChromosome().length(); i++) {
            identifiedComponents.merge(
                    best.getChromosome().getGene(i).getAllele(),
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

    static Double computeFitnessValue(final Genotype<LongGene> prospect) {
        LongObjectiveChromosome chromosome = (LongObjectiveChromosome) prospect.getChromosome();
        Double res = chromosome.getCohesionObjective() + chromosome.getCouplingObjective() + chromosome.getComponentSizeObjective() + chromosome.getComponentRangeObjective() + chromosome.getCohesiveComponentObjective();
        return res;
    }

    private static void update(final EvolutionResult<LongGene, Double> result) {
        int percentage = (int) (1.0 * result.getGeneration() / generations * 100);
        int left = 100 - percentage;
        if (best == null || result.getBestFitness() > bestFitness) {
            bestFitness = result.getBestFitness();
            bestGeneration = result.getGeneration();
            best = result.getBestPhenotype().getGenotype();
        }
        lastGeneration = result.getGeneration();
        Long components = best.getChromosome().stream().mapToLong(l -> l.getAllele()).distinct().count();
        System.out.print("\rProgress: " + Strings.repeat("=", percentage) + Strings.repeat(" ", left) + "| ");
        System.out.print("Best Genotype at Generation: " + bestGeneration + " with Fitness: " + bestFitness + " with Components: " + components);
        if (result.getGeneration() == generations) {
            System.out.println("");
        }
    }
}
