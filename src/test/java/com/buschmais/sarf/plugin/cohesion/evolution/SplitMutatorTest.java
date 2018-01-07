package com.buschmais.sarf.plugin.cohesion.evolution;

import com.buschmais.sarf.plugin.cohesion.evolution.coupling.LongObjectiveCouplingChromosome;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.LongGene;
import io.jenetics.MutatorResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Stephan Pirnbaum
 */
public class SplitMutatorTest {

    private SplitMutator splitMutator;

    private LongObjectiveChromosome chromosome;

    @Before
    public void setUp() {
        this.splitMutator = new SplitMutator(1);
        this.chromosome = LongObjectiveCouplingChromosome.of(
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
        Genotype.of(this.chromosome);
        MutatorResult<Genotype<LongGene>> res = this.splitMutator.mutate(Genotype.of(this.chromosome), 1, null);
        Chromosome<LongGene> chrom = res.getResult().getChromosome();
        assertEquals((long) chrom.getGene(0).getAllele(), 0);
        assertEquals((long) chrom.getGene(1).getAllele(), 0);
        assertEquals((long) chrom.getGene(2).getAllele(), 0);
        assertEquals((long) chrom.getGene(3).getAllele(), 0);
        assertEquals((long) chrom.getGene(4).getAllele(), 2);
        assertEquals((long) chrom.getGene(5).getAllele(), 2);
        assertEquals((long) chrom.getGene(6).getAllele(), 3);
        assertEquals((long) chrom.getGene(7).getAllele(), 3);
        assertEquals((long) chrom.getGene(8).getAllele(), 3);
        assertEquals((long) chrom.getGene(9).getAllele(), 3);
        assertEquals((long) chrom.getGene(10).getAllele(), 1);
        assertEquals((long) chrom.getGene(11).getAllele(), 1);


    }
}
