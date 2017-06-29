package com.buchmais.sarf.classification.criterion.cohesion.evolution;

import org.jenetics.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Stephan Pirnbaum
 */
public class ParetoFrontierSelector implements Selector<LongGene, Double> {

    @Override
    public Population<LongGene, Double> select(Population<LongGene, Double> population, int count, Optimize opt) {
        List<Phenotype<LongGene, Double>> frontier = new LinkedList<>();
        outer:
        for (Phenotype<LongGene, Double> phenotype1 : population) {
            LongObjectiveChromosome chromosome1 = (LongObjectiveChromosome) phenotype1.getGenotype().getChromosome();
            for (Phenotype<LongGene, Double> phenotype2 : population) {
                LongObjectiveChromosome chromosome2 = (LongObjectiveChromosome) phenotype2.getGenotype().getChromosome();
                if (chromosome2.dominates(chromosome1)) continue outer;
            }
            frontier.add(phenotype1);
        }
        if (count < frontier.size()) {
            return new Population<>(frontier.subList(0, count));
        } else {
            return new Population<>(frontier);
        }
    }
}
