package com.buschmais.sarf.core.plugin.cohesion.evolution;

import com.buschmais.sarf.core.plugin.cohesion.ElementCoupling;
import com.buschmais.sarf.core.plugin.cohesion.evolution.coupling.CouplingBasedFitnessFunction;
import com.buschmais.sarf.core.plugin.cohesion.evolution.similarity.SimilarityBasedFitnessFunction;
import com.google.common.collect.Sets;
import io.jenetics.Genotype;
import io.jenetics.LongGene;
import io.jenetics.ext.moea.Vec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for the computation of the fitness value of {@link LongObjectiveChromosome}s.
 */
public abstract class FitnessFunction {

    /**
     * Evaluate the given genotype (whose {@link Genotype#getChromosome()} must return a {@link LongObjectiveChromosome})
     * for its fitness vector consisting of:
     *
     * <li>
     *     <ul>Cohesion - Positive value [0,1]</ul>
     *     <ul>Coupling - Negative value [-1, 0]</ul>
     * </li>
     *
     * @param i The {@link Genotype} to evaluate.
     *
     * @return The fitness vector.
     */
    final Vec<double[]> evaluate(Genotype<LongGene> i) {
        LongObjectiveChromosome chromosome = (LongObjectiveChromosome) i.getChromosome();
        return evaluate(chromosome);
    }

    private Vec<double[]> evaluate(LongObjectiveChromosome chromosome) {
        double cohesionObjective = 0;
        double couplingObjective = 0;
        double componentRangeObjective = 0;
        double cohesiveComponentObjective = 0;
        double componentSizeObjective = 0;

        // mapping from component id to a set of type ids
        Map<Long, Set<Long>> identifiedComponents = new HashMap<>();
        for (int i = 0; i < chromosome.length(); i++) {
            identifiedComponents.merge(
                chromosome.getGene(i).getAllele(),
                Sets.newHashSet(Partitioner.ids[i]),
                (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });
        }
        int uncohesiveComponents = 0;
        int subComponents = 0;
        int totalSubComponents = 0;
        // compute fitness for intra-edge coupling (cohesiveness of components)
        for (Map.Entry<Long, Set<Long>> component1 : identifiedComponents.entrySet()) {
            cohesionObjective += computeCohesion(component1.getValue());
            if ((subComponents = Problem.getInstance().connectedComponents(component1.getValue()).keySet().size()) > 1) {
                uncohesiveComponents++;
                totalSubComponents += subComponents;
            }
        }
        for (Map.Entry<ElementCoupling, ElementCoupling> elementCoupling : chromosome.getComponents().entrySet()) {
            couplingObjective -= elementCoupling.getValue().getCoupling();
        }
        couplingObjective = normalizeCoupling(couplingObjective, identifiedComponents.size());
        cohesionObjective /= identifiedComponents.size();
        // minimize the difference between min and max component size
        componentRangeObjective = ((double) (identifiedComponents.values().stream().mapToInt(Set::size).min().orElse(0) -
            identifiedComponents.values().stream().mapToInt(Set::size).max().orElse(0))) / (Partitioner.ids.length - 1);
        // punish one-type only components
        //punish un-cohesive components
        cohesiveComponentObjective = uncohesiveComponents == 0 ? 1 : (totalSubComponents > identifiedComponents.size() ? 0 : (1 - ((double) totalSubComponents) / identifiedComponents.size()));
        componentSizeObjective = -identifiedComponents.values().stream().mapToInt(Set::size).filter(i -> i == 1).count() / (double) identifiedComponents.size();

        return Vec.of(
            cohesionObjective,
            couplingObjective
            /*,
                    chromosome.getCohesiveComponentObjective(),
                    chromosome.getComponentRangeObjective(),
                    chromosome.getComponentSizeObjective()
            */
        );
    }

    /**
     * Normalize the coupling based on the strategy and component size.
     *
     * @param couplingObjective The non-normalized coupling value.
     * @param size The component size.
     *
     * @return The normalized value in the range [-1, 0]
     *
     * @see CouplingBasedFitnessFunction
     * @see SimilarityBasedFitnessFunction
     */
    protected abstract double normalizeCoupling(double couplingObjective, int size);

    /**
     * Compute the cohesion for the given component based on the strategy.
     *
     * @param elementIds The ids of the element in the component.
     *
     * @return The computed non-normalized cohesion
     *
     * @see CouplingBasedFitnessFunction
     * @see SimilarityBasedFitnessFunction
     */
    protected abstract double computeCohesion(Set<Long> elementIds);
}
