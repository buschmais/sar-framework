package com.buschmais.sarf.plugin.cohesion.evolution.similarity;

import com.buschmais.sarf.plugin.cohesion.evolution.LongObjectiveChromosome;
import com.buschmais.sarf.plugin.cohesion.evolution.Problem;
import io.jenetics.LongGene;
import io.jenetics.util.ISeq;
import io.jenetics.util.LongRange;

import java.util.Collection;

/**
 * @author Stephan Pirnbaum
 */
public class LongObjectiveSimilarityChromosome extends LongObjectiveChromosome {

    protected LongObjectiveSimilarityChromosome(ISeq<LongGene> genes) {
        super(genes);
    }

    public LongObjectiveSimilarityChromosome(Long min, Long max, int length) {
        super(min, max, length);
    }

    public LongObjectiveSimilarityChromosome(Long min, Long max) {
        super(min, max);
    }

    @Override
    protected Double computeCohesion(Collection<Long> ids) {
        int denominator = ids.size() == 1 ? 1 : ((ids.size() * (ids.size() - 1)) / 2);
        return Problem.getInstance().computeCohesionInComponent(ids) / denominator;
    }

    @Override
    protected Double computeCoupling(Collection<Long> ids1, Collection<Long> ids2) {
        return Problem.getInstance().computeCouplingBetweenComponents(ids1, ids2) / (ids1.size() * ids2.size());
    }

    @Override
    protected Double normalizeCoupling(Double coupling, int components) {
        return 2 * coupling / (components * (components - 1));
    }

    @Override
    public LongObjectiveSimilarityChromosome newInstance(ISeq<LongGene> genes) {
        return new LongObjectiveSimilarityChromosome(genes);
    }

    @Override
    public LongObjectiveSimilarityChromosome newInstance() {
        return new LongObjectiveSimilarityChromosome(this.getMin(), this.getMax(), this.length());
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
    public static LongObjectiveSimilarityChromosome of(final LongGene... genes) {
        return new LongObjectiveSimilarityChromosome(ISeq.of(genes));
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
    public static LongObjectiveSimilarityChromosome of(
            final long min,
            final long max,
            final int length
    ) {
        return new LongObjectiveSimilarityChromosome(min, max, length);
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
    public static LongObjectiveSimilarityChromosome of(final LongRange range, final int length) {
        return new LongObjectiveSimilarityChromosome(range.getMin(), range.getMax(), length);
    }

    /**
     * Create a new random {@code LongObjectiveChromosome} of length one.
     *
     * @param min the minimal value of this chromosome (inclusively).
     * @param max the maximal value of this chromosome (inclusively).
     * @return a new {@code LongObjectiveChromosome} with the given gene parameters.
     */
    public static LongObjectiveSimilarityChromosome of(final long min, final long max) {
        return new LongObjectiveSimilarityChromosome(min, max);
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
    public static LongObjectiveSimilarityChromosome of(final LongRange range) {
        return new LongObjectiveSimilarityChromosome(range.getMin(), range.getMax());
    }

}
