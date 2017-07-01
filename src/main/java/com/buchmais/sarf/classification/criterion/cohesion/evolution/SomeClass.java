package com.buchmais.sarf.classification.criterion.cohesion.evolution;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.google.common.collect.Sets;
import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;

import java.util.*;

/**
 * @author Stephan Pirnbaum
 */
public class SomeClass {

    static Genotype<LongGene> best;
    static Double bestFitness;

    protected static long[] ids;

    public SomeClass() {
        SARFRunner.xoManager.currentTransaction().begin();
        List<TypeDescriptor> types = new ArrayList<>();
        try (Result<TypeDescriptor> descriptors = SARFRunner.xoManager.getRepository(TypeRepository.class).getAllInternalTypes()) {
            for (TypeDescriptor t : descriptors) {
                types.add(t);
            }
        }
        ids = types.stream().mapToLong(t -> SARFRunner.xoManager.getId(t)).sorted().toArray();
        SARFRunner.xoManager.currentTransaction().commit();
    }

    public void someMethod() {
        int maximumNumberOfComponents = ids.length;
        Genotype<LongGene> genotype = packageStructureToGenotype();
        final Engine<LongGene, Double> engine = Engine
                .builder(SomeClass::computeFitnessValue, genotype)
                .offspringFraction(0.7)
                .survivorsSelector(new ParetoFrontierSelector())
                .offspringSelector(new ParetoFrontierSelector())
                .populationSize(25)
                .fitnessScaler(f -> Math.pow(f, 5))
                .alterers(new CouplingMutator(0.3), new GaussianMutator<>(0.1), new MultiPointCrossover<>(0.8))
                .executor(Runnable::run)
                .build();
        System.out.println("\n\n\n Starting Evolution \n\n\n");

        List<Genotype<LongGene>> genotypes = Arrays.asList(genotype);

        engine
                .stream()
                .limit(1000)
                .peek(SomeClass::update)
                .collect(EvolutionResult.toBestGenotype());
        // print result
        SARFRunner.xoManager.currentTransaction().begin();
        Map<Long, Set<String>> identifiedComponents = new HashMap<>();
        for (int i = 0; i < best.getChromosome().length(); i++) {
            identifiedComponents.merge(
                    best.getChromosome().getGene(i).getAllele(),
                    Sets.newHashSet(SARFRunner.xoManager.findById(TypeDescriptor.class, ids[i]).getFullQualifiedName()),
                    (s1, s2) -> {
                        s1.addAll(s2);
                        return s1;
                    });
        }
        for (Map.Entry<Long, Set<String>> component : identifiedComponents.entrySet()) {
            System.out.println("Component " + component.getKey());
            for (String s : component.getValue()) {
                System.out.println("\t" + s);
            }
        }
        SARFRunner.xoManager.currentTransaction().close();
    }

    private Genotype<LongGene> packageStructureToGenotype() {
        // Package name to type ids
        Map<String, Set<Long>> packageComponents = new HashMap<>();
        TypeRepository typeRepository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        for (Long id : ids) {
            String packageName = typeRepository.getPackageName(id);
            packageComponents.merge(
                    packageName,
                    Sets.newHashSet(id),
                    (s1, s2) -> {
                        s1.addAll(s2);
                        return s1;
                    });
        }
        List<LongGene> genes = new LinkedList<>();
        for (Long id : ids) {
            int componentId = 0;
            for (Map.Entry<String, Set<Long>> entry : packageComponents.entrySet()) {
                if (entry.getValue().contains(id)) {
                    genes.add(LongGene.of(componentId, 0, 45));
                }
                componentId++;
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
