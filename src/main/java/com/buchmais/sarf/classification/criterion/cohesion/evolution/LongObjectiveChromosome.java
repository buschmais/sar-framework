package com.buchmais.sarf.classification.criterion.cohesion.evolution;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.repository.MetricRepository;
import com.google.common.collect.Sets;
import org.jenetics.LongChromosome;
import org.jenetics.LongGene;
import org.jenetics.util.ISeq;
import org.jenetics.util.LongRange;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public class LongObjectiveChromosome extends LongChromosome {

    private boolean evaluated = false;

    private Double cohesionObjective = 0d;

    private Double couplingObjective = 0d;

    private int componentCountObjective = 0;

    private long componentSizeObjective = 0;

    private int componentRangeObjective = 0;

    protected LongObjectiveChromosome(ISeq<LongGene> genes) {
        super(genes);
    }

    public LongObjectiveChromosome(Long min, Long max, int length) {
        super(min, max, length);
    }

    public LongObjectiveChromosome(Long min, Long max) {
        super(min, max);
    }

    private void evaluate() {
        // mapping from component id to a set of type ids
        Map<Long, Set<Long>> identifiedComponents = new HashMap<>();
        for (int i = 0; i < this.length(); i++) {
            identifiedComponents.merge(
                    this.getGene(i).getAllele(),
                    Sets.newHashSet(SomeClass.ids[i]),
                    (s1, s2) -> {
                        s1.addAll(s2);
                        return s1;
                    });
        }
        SARFRunner.xoManager.currentTransaction().begin();
        MetricRepository mR = SARFRunner.xoManager.getRepository(MetricRepository.class);
        // compute fitness for intra-edge coupling (cohesiveness of components)
        for (Map.Entry<Long, Set<Long>> component1 : identifiedComponents.entrySet()) {
            long[] ids1 = component1.getValue().stream().mapToLong(i -> i).toArray();
            this.cohesionObjective += mR.computeCohesionInComponent(
                    ids1
            );
            // compute fitness for inter-edge coupling (coupling of components)
            // is compared twice -> punishing inter-edges
            for (Map.Entry<Long, Set<Long>> component2 : identifiedComponents.entrySet()) {
                long[] ids2 = component2.getValue().stream().mapToLong(i -> i).toArray();
                if (!Objects.equals(component1.getKey(), component2.getKey())) {
                    this.couplingObjective -= mR.computeCouplingBetweenComponents(
                            ids2,
                            ids1
                    );
                }
            }
        }
        SARFRunner.xoManager.currentTransaction().commit();
        // minimize the difference between min and max component size
        this.componentRangeObjective = identifiedComponents.values().stream().mapToInt(Set::size).min().orElse(0) -
                identifiedComponents.values().stream().mapToInt(Set::size).max().orElse(0);
        // punish one-type only components
        this.componentSizeObjective = identifiedComponents.values().stream().mapToInt(Set::size).filter(i -> i == 1).count();
        // maximize component number
        this.componentCountObjective = identifiedComponents.size();
        this.evaluated = true;

    }

    protected Double getCohesionObjective() {
        if (!this.evaluated) evaluate();
        return this.cohesionObjective;
    }

    protected Double getCouplingObjective() {
        if (!this.evaluated) evaluate();
        return this.couplingObjective;
    }

    protected Long getComponentSizeObjective() {
        if (!this.evaluated) evaluate();
        return this.componentSizeObjective;
    }

    protected Integer getComponentRangeObjective() {
        if (!this.evaluated) evaluate();
        return this.componentRangeObjective;
    }

    protected Integer getComponentCountObjective() {
        if (!this.evaluated) evaluate();
        return this.componentCountObjective;
    }

    protected boolean dominates(LongObjectiveChromosome chromosome) {
        if (!evaluated) this.evaluate();
        if (this.cohesionObjective < chromosome.cohesionObjective) return false;
        if (this.couplingObjective < chromosome.couplingObjective) return false;
        if (this.componentSizeObjective < chromosome.componentSizeObjective) return false;
        if (this.componentCountObjective < chromosome.componentCountObjective) return false;
        if (this.componentRangeObjective < chromosome.componentRangeObjective) return false;
        return (this.cohesionObjective > chromosome.cohesionObjective) ||
               (this.couplingObjective > chromosome.couplingObjective) ||
               (this.componentSizeObjective > chromosome.componentSizeObjective) ||
               (this.componentRangeObjective > chromosome.componentRangeObjective) ||
               (this.componentCountObjective > chromosome.componentCountObjective);
    }

    @Override
    public LongObjectiveChromosome newInstance(ISeq<LongGene> genes) {
        return new LongObjectiveChromosome(genes);
    }

    @Override
    public LongObjectiveChromosome newInstance() {
        return new LongObjectiveChromosome(this.getMin(), this.getMax(), this.length());
    }

    /**
     * Create a new {@code LongObjectiveChromosome} with the given genes.
     *
     * @param genes the genes of the chromosome.
     * @return a new chromosome with the given genes.
     * @throws IllegalArgumentException if the length of the genes array is
     *         empty.
     * @throws NullPointerException if the given {@code genes} are {@code null}
     */
    public static LongObjectiveChromosome of(final LongGene... genes) {
        return new LongObjectiveChromosome(ISeq.of(genes));
    }

    /**
     * Create a new random {@code LongObjectiveChromosome}.
     *
     * @param min the min value of the {@link LongGene}s (inclusively).
     * @param max the max value of the {@link LongGene}s (inclusively).
     * @param length the length of the chromosome.
     * @return a new {@code LongObjectiveChromosome} with the given gene parameters.
     * @throws IllegalArgumentException if the {@code length} is smaller than
     *         one.
     */
    public static LongObjectiveChromosome of(
            final long min,
            final long max,
            final int length
    ) {
        return new LongObjectiveChromosome(min, max, length);
    }

    /**
     * Create a new random {@code LongObjectiveChromosome}.
     *
     * @since 3.2
     *
     * @param range the long range of the chromosome.
     * @param length the length of the chromosome.
     * @return a new random {@code LongObjectiveChromosome}
     * @throws NullPointerException if the given {@code range} is {@code null}
     * @throws IllegalArgumentException if the {@code length} is smaller than
     *         one.
     */
    public static LongObjectiveChromosome of(final LongRange range, final int length) {
        return new LongObjectiveChromosome(range.getMin(), range.getMax(), length);
    }

    /**
     * Create a new random {@code LongObjectiveChromosome} of length one.
     *
     * @param min the minimal value of this chromosome (inclusively).
     * @param max the maximal value of this chromosome (inclusively).
     * @return a new {@code LongObjectiveChromosome} with the given gene parameters.
     */
    public static LongObjectiveChromosome of(final long min, final long max) {
        return new LongObjectiveChromosome(min, max);
    }

    /**
     * Create a new random {@code LongObjectiveChromosome} of length one.
     *
     * @since 3.2
     *
     * @param range the long range of the chromosome.
     * @return a new random {@code LongObjectiveChromosome} of length one
     * @throws NullPointerException if the given {@code range} is {@code null}
     */
    public static LongObjectiveChromosome of(final LongRange range) {
        return new LongObjectiveChromosome(range.getMin(), range.getMax());
    }
}
