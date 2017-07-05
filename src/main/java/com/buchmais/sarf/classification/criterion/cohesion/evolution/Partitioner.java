package com.buchmais.sarf.classification.criterion.cohesion.evolution;

import com.buchmais.sarf.SARFRunner;
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

    public static Map<Long, Set<Long>> partition(long[] ids, Map<Long, Set<Long>> initialPartitioning) {
        Partitioner.best = null;
        Partitioner.bestFitness = Double.MIN_VALUE;
        Partitioner.ids = ids;
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
                .limit(5)
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
                    genes.add(LongGene.of(compId, 0, ids.length / 2));
                    found = true;
                }
                compId++;
            }
            if (!found) {
                genes.add(LongGene.of(compId, 0, ids.length / 2));
            }
        }
        Chromosome<LongGene> chromosome = LongObjectiveChromosome.of(genes.toArray(new LongGene[genes.size()]));
        System.out.println(chromosome);
        System.out.println(computeFitnessValue(Genotype.of(chromosome)));
        return Genotype.of(chromosome);
    }

    static Double computeFitnessValue(final Genotype<LongGene> prospect) {
        LongObjectiveChromosome chromosome = (LongObjectiveChromosome) prospect.getChromosome();
        return chromosome.getCohesionObjective() + chromosome.getCouplingObjective() + chromosome.getComponentCountObjective() + chromosome.getComponentRangeObjective() + chromosome.getComponentSizeObjective();
    }

    private static void update(final EvolutionResult<LongGene, Double> result) {
        System.out.println("Generation: " + result.getGeneration() + "\nBest Fitness: " + result.getBestFitness() + "\n Size: " + result.getPopulation().size());
        LongObjectiveChromosome chromosome = (LongObjectiveChromosome) result.getBestPhenotype().getGenotype().getChromosome();
        System.out.println(chromosome.getCohesionObjective() + " " +
                chromosome.getCouplingObjective() + " " +
                chromosome.getComponentCountObjective() + " " +
                chromosome.getComponentRangeObjective() + " " +
                chromosome.getComponentSizeObjective());
        if (best == null || result.getBestFitness() > bestFitness) {
            best = result.getBestPhenotype().getGenotype();
            bestFitness = result.getBestFitness();
        }
    }
}
