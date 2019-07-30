package com.buschmais.sarf.core.plugin.cohesion.evolution;

import com.buschmais.sarf.core.plugin.cohesion.ElementCoupling;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jenetics.LongChromosome;
import io.jenetics.LongGene;
import io.jenetics.internal.math.random;
import io.jenetics.util.ISeq;
import io.jenetics.util.IntRange;
import io.jenetics.util.MSeq;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static io.jenetics.util.RandomRegistry.getRandom;
import static java.lang.String.format;

/**
 * @author Stephan Pirnbaum
 */
public final class LongObjectiveChromosome extends LongChromosome {

    @Getter
    private Map<ElementCoupling, ElementCoupling> components = new HashMap<>();

    @Getter
    private final Map<Long, Long> elementToComponent = new HashMap<>();

    /**
     * Creates a new {@link LongObjectiveChromosome} from the given {@link LongGene}s.
     *
     * @param genes The {@link LongGene}s.
     */
    LongObjectiveChromosome(ISeq<LongGene> genes) {
        super(genes, IntRange.of(genes.length()));
        init();
    }

    private void init() {
        final Multimap<Long, Long> componentToElements = HashMultimap.create();
        for (int i = 0; i < this.length(); i++) {
            long componentId = this.getGene(i).getAllele();
            long containedId = Partitioner.ids[i];
            elementToComponent.put(containedId, componentId);
            componentToElements.put(componentId, containedId);
        }
        Map<ElementCoupling, ElementCoupling> couplings = Problem.getInstance().couplings;
        // for each coupling
        for (Map.Entry<ElementCoupling, ElementCoupling> coupling : couplings.entrySet()) {
            // if elements are in different components
            long sourceComponent = elementToComponent.get(coupling.getValue().getSource());
            long targetComponent = elementToComponent.get(coupling.getValue().getTarget());
            if (sourceComponent != targetComponent) {
                // add coupling between components
                ElementCoupling elementCoupling = new ElementCoupling(sourceComponent, targetComponent);
                components.putIfAbsent(elementCoupling, elementCoupling);
                components.get(elementCoupling).addCoupling(coupling.getValue().getCoupling());
            }
        }
        // normalize coupling based on component sizes
        for (ElementCoupling componentCoupling : this.components.values()) {
            int sourceComponentSize = componentToElements.get(componentCoupling.getSource()).size();
            int targetComponentSize = componentToElements.get(componentCoupling.getTarget()).size();
            int denominator = sourceComponentSize * targetComponentSize;
            componentCoupling.normalizeCoupling(denominator);
        }
    }

    @Override
    public LongObjectiveChromosome newInstance(ISeq<LongGene> genes) {
        return new LongObjectiveChromosome(genes);
    }

    @Override
    public LongObjectiveChromosome newInstance() {
        final Random r = getRandom();
        ISeq<LongGene> longGenes = MSeq.<LongGene>ofLength(random.nextInt(IntRange.of(this.length()), r))
            .fill(() -> LongGene.of(nextLong(r, this.getMin(), this.getMax()), this.getMin(), this.getMax()))
            .toISeq();
        return new LongObjectiveChromosome(longGenes);
    }

    /**
     * Returns a pseudo-random, uniformly distributed int value between min
     * and max (min and max included).
     *
     * @param random the random engine to use for calculating the random
     *        long value
     * @param min lower bound for generated long integer
     * @param max upper bound for generated long integer
     * @return a random long integer greater than or equal to {@code min}
     *         and less than or equal to {@code max}
     * @throws IllegalArgumentException if {@code min > max}
     * @throws NullPointerException if the given {@code random}
     *         engine is {@code null}.
     */
    private static long nextLong(
        final Random random,
        final long min, final long max
    ) {
        if (min > max) {
            throw new IllegalArgumentException(format(
                "min >= max: %d >= %d.", min, max
            ));
        }

        final long diff = (max - min) + 1;
        long result = 0;

        if (diff <= 0) {
            do {
                result = random.nextLong();
            } while (result < min || result > max);
        } else if (diff < Integer.MAX_VALUE) {
            result = random.nextInt((int)diff) + min;
        } else {
            result = nextLong(random, diff) + min;
        }

        return result;
    }

    /**
     * Returns a pseudo-random, uniformly distributed int value between 0
     * (inclusive) and the specified value (exclusive), drawn from the given
     * random number generator's sequence.
     *
     * @param random the random engine used for creating the random number.
     * @param n the bound on the random number to be returned. Must be
     *        positive.
     * @return the next pseudo-random, uniformly distributed int value
     *         between 0 (inclusive) and n (exclusive) from the given random
     *         number generator's sequence
     * @throws IllegalArgumentException if n is smaller than 1.
     * @throws NullPointerException if the given {@code random}
     *         engine is {@code null}.
     */
    private static long nextLong(final Random random, final long n) {
        if (n <= 0) {
            throw new IllegalArgumentException(format(
                "n is smaller than one: %d", n
            ));
        }

        long bits;
        long result;
        do {
            bits = random.nextLong() & 0x7fffffffffffffffL;
            result = bits%n;
        } while (bits - result + (n - 1) < 0);

        return result;
    }

}
