package com.buchmais.sarf.classification.criterion.cohesion.evolution;

import org.jenetics.LongChromosome;
import org.jenetics.LongGene;
import org.jenetics.util.MSeq;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Stephan Pirnbaum
 */
public class SplitMutatorTest {

    private SplitMutator splitMutator;

    private LongChromosome chromosome;

    @Before
    public void setUp() {
        this.splitMutator = new SplitMutator(1);
        this.chromosome = LongChromosome.of(
                LongGene.of(0, 0, 11),
                LongGene.of(0, 0, 11),
                LongGene.of(0, 0, 11),
                LongGene.of(0, 0, 11),
                LongGene.of(0, 0, 11),
                LongGene.of(0, 0, 11),
                LongGene.of(0, 0, 11),
                LongGene.of(0, 0, 11),
                LongGene.of(0, 0, 11),
                LongGene.of(0, 0, 11),
                LongGene.of(1, 0, 11),
                LongGene.of(1, 0, 11)
        );
        Partitioner.ids = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        Problem p = Problem.newInstance(13, 13, false);
        p.addRelation(1, 2, 1);
        p.addRelation(1, 3, 1);
        p.addRelation(2, 3, 1);
        p.addRelation(3, 4, 1);
        p.addRelation(5, 6, 1);
        p.addRelation(7, 8, 1);
        p.addRelation(7, 9, 1);
        p.addRelation(8, 9, 1);
        p.addRelation(9, 10, 1);
        p.addRelation(11, 12, 1);

    }

    @Test
    public void testSplit() {
        MSeq<LongGene> seq = MSeq.of(this.chromosome.toSeq());
        this.splitMutator.mutate(seq, 1);
        assertEquals((long) seq.get(0).getAllele(), 0);
        assertEquals((long) seq.get(1).getAllele(), 0);
        assertEquals((long) seq.get(2).getAllele(), 0);
        assertEquals((long) seq.get(3).getAllele(), 0);
        assertEquals((long) seq.get(4).getAllele(), 2);
        assertEquals((long) seq.get(5).getAllele(), 2);
        assertEquals((long) seq.get(6).getAllele(), 3);
        assertEquals((long) seq.get(7).getAllele(), 3);
        assertEquals((long) seq.get(8).getAllele(), 3);
        assertEquals((long) seq.get(9).getAllele(), 3);
        assertEquals((long) seq.get(10).getAllele(), 1);
        assertEquals((long) seq.get(11).getAllele(), 1);


    }
}
