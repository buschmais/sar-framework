package com.buchmais.sarf.classification.criterion.cohesion;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.repository.MetricRepository;
import com.google.common.collect.Sets;
import org.jenetics.LongChromosome;
import org.jenetics.LongGene;
import org.jenetics.util.ISeq;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public abstract class LongObjectiveChromosome extends LongChromosome {

    private boolean evaluated = false;

    private Double cohesionObjective = 0d;

    private Double couplingObjective = 0d;

    private Double componentCountObjective = 0d;

    private Double componentSizeObjective = 0d;

    private Double componentRangeObjective = 0d;

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
                    Sets.newHashSet(Partitioner.ids[i]),
                    (s1, s2) -> {
                        s1.addAll(s2);
                        return s1;
                    });
        }
        //SARFRunner.xoManager.currentTransaction().begin();
        MetricRepository mR = SARFRunner.xoManager.getRepository(MetricRepository.class);
        // compute fitness for intra-edge coupling (cohesiveness of components)
        for (Map.Entry<Long, Set<Long>> component1 : identifiedComponents.entrySet()) {
            long[] ids1 = component1.getValue().stream().mapToLong(i -> i).toArray();
            this.cohesionObjective += computeCohesion(mR, ids1);
            // compute fitness for inter-edge coupling (coupling of components)
            // is compared twice -> punishing inter-edges
            for (Map.Entry<Long, Set<Long>> component2 : identifiedComponents.entrySet()) {
                long[] ids2 = component2.getValue().stream().mapToLong(i -> i).toArray();
                if (!Objects.equals(component1.getKey(), component2.getKey())) {
                    this.couplingObjective -= computeCoupling(mR, ids1, ids2);
                }
            }
        }
        this.couplingObjective /= (identifiedComponents.size() * (identifiedComponents.size() - 1)) / 2; // TODO: 22.07.2017 Improve
        this.cohesionObjective /= identifiedComponents.size();
        //SARFRunner.xoManager.currentTransaction().commit();
        // minimize the difference between min and max component size
        this.componentRangeObjective = ((double) (identifiedComponents.values().stream().mapToInt(Set::size).min().orElse(0) -
                identifiedComponents.values().stream().mapToInt(Set::size).max().orElse(0))) / (Partitioner.ids.length - 1);
        // punish one-type only components
        this.componentSizeObjective = - identifiedComponents.values().stream().mapToInt(Set::size).filter(i -> i == 1).count() / (double) identifiedComponents.size();
        // maximize component number
        this.componentCountObjective = ((double) identifiedComponents.size()) / Partitioner.ids.length;
        System.out.println(identifiedComponents.size() + " " + this.cohesionObjective + " " + this.couplingObjective + " " +
            this.componentRangeObjective + " " + this.componentSizeObjective + " " + this.componentCountObjective);
        this.evaluated = true;

    }

    abstract Double computeCohesion(MetricRepository mR, long[] ids);

    abstract Double computeCoupling(MetricRepository mR, long[] ids1, long[] ids2);

    protected Double getCohesionObjective() {
        if (!this.evaluated) evaluate();
        return this.cohesionObjective;
    }

    protected Double getCouplingObjective() {
        if (!this.evaluated) evaluate();
        return this.couplingObjective;
    }

    protected Double getComponentSizeObjective() {
        if (!this.evaluated) evaluate();
        return this.componentSizeObjective;
    }

    protected Double getComponentRangeObjective() {
        if (!this.evaluated) evaluate();
        return this.componentRangeObjective;
    }

    protected Double getComponentCountObjective() {
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
}
