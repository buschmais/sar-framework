package com.buchmais.sarf.classification.criterion.logic.cohesion.evolution;

import com.buchmais.sarf.SARFRunner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;

import java.util.*;

/**
 * @author Stephan Pirnbaum
 */
public class Partitioner {

    static Genotype<LongGene> best;
    static Double bestFitness;

    static long[] ids;

    static int generations;
    private static long bestGeneration;

    public static Map<Long, Set<Long>> partition(long[] ids, Map<Long, Set<Long>> initialPartitioning, int generations) {
        Partitioner.best = null;
        Partitioner.bestFitness = Double.MIN_VALUE;
        Partitioner.ids = ids;
        Partitioner.generations = generations;
        Genotype<LongGene> genotype = createGenotype(initialPartitioning);
        final Engine<LongGene, Double> engine = Engine
                .builder(Partitioner::computeFitnessValue, genotype)
                .offspringFraction(0.7)
                .survivorsSelector(new ParetoFrontierSelector())
                .offspringSelector(new ParetoFrontierSelector())
                .populationSize(25)
                .fitnessScaler(f -> Math.pow(f, 5))
                .alterers(new CouplingMutator(0.3), new GaussianMutator<>(0.1), new MultiPointCrossover<>(0.8))
                .executor(Runnable::run)
                .build();
        List<Genotype<LongGene>> genotypes = Arrays.asList(genotype);

        engine
                .stream(genotypes)
                .limit(generations)
                .peek(Partitioner::update)
                .collect(EvolutionResult.toBestGenotype());
        // print result
        SARFRunner.xoManager.currentTransaction().begin();
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
        SARFRunner.xoManager.currentTransaction().close();
        return identifiedComponents;

    }

    private static Genotype<LongGene> createGenotype(Map<Long, Set<Long>> initialPartitioning) {
        List<LongGene> genes = new LinkedList<>();
        for (Long id : ids) {
            int compId = 0;
            boolean found = false;
            for (Map.Entry<Long, Set<Long>> entry : initialPartitioning.entrySet()) {
                if (entry.getValue().contains(id)) {
                    genes.add(LongGene.of(compId, 0, ids.length - 2));
                    found = true;
                }
                compId++;
            }
            if (!found) {
                genes.add(LongGene.of(compId, 0, ids.length - 2));
            }
        }
        Chromosome<LongGene> chromosome = LongObjectiveChromosome.of(genes.toArray(new LongGene[genes.size()]));
        return Genotype.of(chromosome);
    }

    static Double computeFitnessValue(final Genotype<LongGene> prospect) {
        LongObjectiveChromosome chromosome = (LongObjectiveChromosome) prospect.getChromosome();
        return chromosome.getCohesionObjective() + chromosome.getCouplingObjective() + chromosome.getComponentCountObjective() + chromosome.getComponentRangeObjective() + chromosome.getComponentSizeObjective();
    }

    private static void update(final EvolutionResult<LongGene, Double> result) {
        int percentage = (int) (1.0 * result.getGeneration() / generations * 100);
        int left = 100 - percentage;
        if (best == null || result.getBestFitness() > bestFitness) {
            bestFitness = result.getBestFitness();
            bestGeneration = result.getGeneration();
            best = result.getBestPhenotype().getGenotype();
        }
        Long components = best.getChromosome().stream().mapToLong(l -> l.getAllele()).distinct().count();
        System.out.print("\rProgress: " + Strings.repeat("=", percentage) + Strings.repeat(" ", left) + "| ");
        System.out.print("Best Genotype at Generation: " + bestGeneration + " with Fitness: " + bestFitness + " with Components: " + components);
        if (result.getGeneration() == generations) {
            System.out.println("");
        }
    }
}
