package com.buchmais.sarf.classification.criterion.cohesion.evolution;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.node.ComponentDescriptor;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
public class Partitioner {

    static Genotype<LongGene> best;
    static Double bestFitness;

    static long[] ids;

    public Partitioner() {
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

    public Map<Long, Set<Long>> partition(Set<ComponentDescriptor> initialPartitioning) {
        Partitioner.best = null;
        Partitioner.bestFitness = Double.MIN_VALUE;
        Genotype<LongGene> genotype;
        if (initialPartitioning == null || initialPartitioning.size() == 0) {
            genotype = packageStructureToGenotype();
        } else {
            genotype = componentStructureToGenotype(initialPartitioning);
        }
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
        System.out.println("\n\n\n Starting Evolution \n\n\n");

        List<Genotype<LongGene>> genotypes = Arrays.asList(genotype);

        engine
                .stream()
                .limit(150)
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

    private Genotype<LongGene> componentStructureToGenotype(Set<ComponentDescriptor> components) {
        Map<Long, Set<Long>> componentMappings = new HashMap<>();
        SARFRunner.xoManager.currentTransaction().begin();
        ComponentRepository componentRepository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
        // Type ID -> Component IDs
        Map<Long, Set<Long>> typeToComponents = new HashMap<>();
        Set<String> shapes = components.stream().map(ComponentDescriptor::getShape).collect(Collectors.toSet());
        for (Long typeId : ids) {
            for (String shape : shapes) {
                Long bestCId = componentRepository.getBestComponentForShape(components.stream().mapToLong(c -> SARFRunner.xoManager.getId(c)).toArray(),
                        shape, typeId);
                if (bestCId != null) {
                    typeToComponents.merge(
                            typeId,
                            Sets.newHashSet(bestCId),
                            (c1, c2) -> {
                                c1.addAll(c2);
                                return c1;
                            }
                    );
                }
            }
        }
        ArrayListMultimap<Set<Long>, Long> inverse = Multimaps.invertFrom(Multimaps.forMap(typeToComponents), ArrayListMultimap.create());
        Long componentId = 0L;
        for (Set<Long> componentIds : inverse.keys().elementSet()) {
            componentMappings.put(componentId, Sets.newHashSet(inverse.get(componentIds)));
            componentId++;
        }
        SARFRunner.xoManager.currentTransaction().commit();
        List<LongGene> genes = new LinkedList<>();
        for (Long id : ids) {
            int compId = 0;
            boolean found = false;
            for (Map.Entry<Long, Set<Long>> entry : componentMappings.entrySet()) {
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
                    genes.add(LongGene.of(componentId, 0, ids.length / 2));
                    break;
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
