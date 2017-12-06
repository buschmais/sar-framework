package com.buschmais.sarf.plugin.cohesion.evolution.coupling;

import com.buschmais.sarf.plugin.cohesion.evolution.LongObjectiveChromosome;
import com.buschmais.sarf.plugin.cohesion.evolution.Problem;
import org.jenetics.LongGene;
import org.jenetics.util.ISeq;
import org.jenetics.util.LongRange;

import java.util.Collection;

/**
 * @author Stephan Pirnbaum
 */
public class LongObjectiveCouplingChromosome extends LongObjectiveChromosome {

    protected LongObjectiveCouplingChromosome(ISeq<LongGene> genes) {
        super(genes);
    }

    public LongObjectiveCouplingChromosome(Long min, Long max, int length) {
        super(min, max, length);
    }

    public LongObjectiveCouplingChromosome(Long min, Long max) {
        super(min, max);
    }

    @Override
    protected Double computeCohesion(Collection<Long> ids) {
        int denominator = ids.size() == 1 ? 1 : ((ids.size() * (ids.size() - 1)) / 2);
        return Problem.getInstance().computeCohesionInComponent(ids) / denominator;
    }

    @Override
    protected Double computeCoupling(Collection<Long> ids1, Collection<Long> ids2) {
        return Problem.getInstance().computeCouplingBetweenComponents(ids1, ids2) / (2 * ids1.size() * ids2.size());
    }

    @Override
    protected Double normalizeCoupling(Double coupling, int components) {
        return coupling / (components * (components - 1));
    }

    @Override
    public LongObjectiveCouplingChromosome newInstance(ISeq<LongGene> genes) {
        return new LongObjectiveCouplingChromosome(genes);
    }

    @Override
    public LongObjectiveCouplingChromosome newInstance() {
        return new LongObjectiveCouplingChromosome(this.getMin(), this.getMax(), this.length());
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
    public static LongObjectiveCouplingChromosome of(final LongGene... genes) {
        return new LongObjectiveCouplingChromosome(ISeq.of(genes));
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
    public static LongObjectiveCouplingChromosome of(
            final long min,
            final long max,
            final int length
    ) {
        return new LongObjectiveCouplingChromosome(min, max, length);
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
    public static LongObjectiveCouplingChromosome of(final LongRange range, final int length) {
        return new LongObjectiveCouplingChromosome(range.getMin(), range.getMax(), length);
    }

    /**
     * Create a new random {@code LongObjectiveChromosome} of length one.
     *
     * @param min the minimal value of this chromosome (inclusively).
     * @param max the maximal value of this chromosome (inclusively).
     * @return a new {@code LongObjectiveChromosome} with the given gene parameters.
     */
    public static LongObjectiveCouplingChromosome of(final long min, final long max) {
        return new LongObjectiveCouplingChromosome(min, max);
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
    public static LongObjectiveCouplingChromosome of(final LongRange range) {
        return new LongObjectiveCouplingChromosome(range.getMin(), range.getMax());
    }
}
