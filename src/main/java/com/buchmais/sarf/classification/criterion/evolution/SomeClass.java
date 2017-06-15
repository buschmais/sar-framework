package com.buchmais.sarf.classification.criterion.evolution;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.repository.MetricRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.google.common.collect.Sets;
import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.util.Factory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
public class SomeClass {

    static List<TypeDescriptor> types;

    protected static long[] ids;

    public SomeClass() {
        types = new ArrayList<>();
        SARFRunner.xoManager.currentTransaction().begin();
        for (TypeDescriptor t : SARFRunner.xoManager.getRepository(TypeRepository.class).getAllInternalTypes()) {
            types.add(t);
        }
        ids = types.stream().mapToLong(t -> SARFRunner.xoManager.getId(t)).sorted().toArray();
        SARFRunner.xoManager.currentTransaction().commit();
    }

    public void someMethod() {
        SARFRunner.xoManager.currentTransaction().begin();
        int maximumNumberOfComponents = types.size();
        Genotype<LongGene> genotype = packageStructureToGenotype();
        final Engine<LongGene, Double> engine = Engine
                .builder(SomeClass::computeFitnessValue, genotype)
                .offspringFraction(0.7)
                .survivorsSelector(new RouletteWheelSelector<>())
                .offspringSelector(new TournamentSelector<>())
                .populationSize(10)
                .fitnessScaler(f -> Math.pow(f, 5))
                .alterers(new CouplingMutator(0.5), new GaussianMutator<>(0.1), new MultiPointCrossover<>(0.8))
                .build();
        System.out.println("\n\n\n Starting Evolution \n\n\n");

        List<Genotype<LongGene>> genotypes = Arrays.asList(genotype);

        final Genotype<LongGene> result = engine
                .stream(genotypes)
                .limit(150)
                .peek(SomeClass::update)
                .collect(EvolutionResult.toBestGenotype());
        // print result
        Map<Long, Set<String>> identifiedComponents = new HashMap<>();
        for (int i = 0; i < result.getChromosome().length(); i++) {
            identifiedComponents.merge(
                    result.getChromosome().getGene(i).getAllele(),
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
    }

    private Genotype<LongGene> packageStructureToGenotype() {
        // Package name to type ids
        Map<String, Set<Long>> packageComponents = new HashMap<>();
        for (TypeDescriptor t : types) {
            String packageName = t.getFullQualifiedName().substring(0, t.getFullQualifiedName().lastIndexOf("."));
            packageComponents.merge(
                    packageName,
                    Sets.newHashSet((Long) SARFRunner.xoManager.getId(t)),
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
                    genes.add(LongGene.of(componentId, 0, 99));
                }
                componentId++;
            }
        }
        Chromosome<LongGene> chromosome = LongChromosome.of(genes.toArray(new LongGene[genes.size()]));
        System.out.println(chromosome);
        System.out.println(computeFitnessValue(Genotype.of(chromosome)));
        return Genotype.of(chromosome);
    }

    static Double computeFitnessValue(final Genotype<LongGene> prospect) {
        Double fitnessValue = 0d;
        LongChromosome chromosome = (LongChromosome) prospect.getChromosome();
        // mapping from component id to a set of type ids
        Map<Long, Set<Long>> identifiedComponents = new HashMap<>();
        for (int i = 0; i < chromosome.length(); i++) {
            identifiedComponents.merge(
                    chromosome.getGene(i).getAllele(),
                    Sets.newHashSet(ids[i]),
                    (s1, s2) -> {
                        s1.addAll(s2);
                        return s1;
                    });
        }
        MetricRepository mR = SARFRunner.xoManager.getRepository(MetricRepository.class);
        // compute fitness for intra-edge coupling (cohesiveness of components)
        for (Map.Entry<Long, Set<Long>> component1 : identifiedComponents.entrySet()) {
            long[] ids1 = component1.getValue().stream().mapToLong(i -> i).toArray();
            fitnessValue += mR.computeCohesionInComponent(
                    ids1
            );
            // compute fitness for inter-edge coupling (coupling of components)
            // is compared twice -> punishing inter-edges
            for (Map.Entry<Long, Set<Long>> component2 : identifiedComponents.entrySet()) {
                long[] ids2 = component2.getValue().stream().mapToLong(i -> i).toArray();
                if (!Objects.equals(component1.getKey(), component2.getKey())) {
                    fitnessValue -= mR.computeCouplingBetweenComponents(
                            ids2,
                            ids1
                    );
                }
            }
        }
        return fitnessValue;
    }

    private static void update(final EvolutionResult<LongGene, Double> result) {
        System.out.println("Generation: " + result.getGeneration() + "\nBest Fitness" + result.getBestFitness() + "\n Size: " + result.getPopulation().size());
        System.out.println(Arrays.toString(ids));
        System.out.println(result.getBestPhenotype());
    }
}
