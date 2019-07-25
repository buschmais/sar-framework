package com.buschmais.sarf.core.plugin.cohesion.evolution;

import com.buschmais.sarf.core.plugin.cohesion.ElementCoupling;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import io.jenetics.LongChromosome;
import io.jenetics.LongGene;
import io.jenetics.util.ISeq;
import io.jenetics.util.IntRange;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public abstract class LongObjectiveChromosome extends LongChromosome {

    private boolean evaluated = false;

    private double cohesionObjective = 0d;

    private double couplingObjective = 0d;

    private double componentSizeObjective = 0d;

    private double componentRangeObjective = 0d;

    private double cohesiveComponentObjective = 0d;

    private Map<ElementCoupling, ElementCoupling> components = new HashMap<>();

    @Getter
    private final Map<Long, Long> elementToComponent = new HashMap<>();

    protected LongObjectiveChromosome(ISeq<LongGene> genes) {
        super(genes, IntRange.of(genes.length()));
        init();
    }

    public LongObjectiveChromosome(Long min, Long max, int length) {
        super(min, max, length);
        init();
    }

    public LongObjectiveChromosome(Long min, Long max) {
        super(min, max);
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
        int uncohesiveComponents = 0;
        int subComponents = 0;
        int totalSubComponents = 0;
        // compute fitness for intra-edge coupling (cohesiveness of components)
        for (Map.Entry<Long, Set<Long>> component1 : identifiedComponents.entrySet()) {
            this.cohesionObjective += computeCohesion(component1.getValue());
            if ((subComponents = Problem.getInstance().connectedComponents(component1.getValue()).keySet().size()) > 1) {
                uncohesiveComponents++;
                totalSubComponents += subComponents;
            }
        }
        for (Map.Entry<ElementCoupling, ElementCoupling> elementCoupling : this.components.entrySet()) {
            this.couplingObjective -= elementCoupling.getValue().getCoupling();
        }
        this.couplingObjective = normalizeCoupling(this.couplingObjective, identifiedComponents.size());
        this.cohesionObjective /= identifiedComponents.size();
        // minimize the difference between min and max component size
        this.componentRangeObjective = ((double) (identifiedComponents.values().stream().mapToInt(Set::size).min().orElse(0) -
            identifiedComponents.values().stream().mapToInt(Set::size).max().orElse(0))) / (Partitioner.ids.length - 1);
        // punish one-type only components
        //punish un-cohesive components
        this.cohesiveComponentObjective = uncohesiveComponents == 0 ? 1 : (totalSubComponents > identifiedComponents.size() ? 0 : (1 - ((double) totalSubComponents) / identifiedComponents.size()));
        this.componentSizeObjective = -identifiedComponents.values().stream().mapToInt(Set::size).filter(i -> i == 1).count() / (double) identifiedComponents.size();
        this.evaluated = true;

    }

    protected abstract double computeCohesion(Collection<Long> ids);

    protected abstract double normalizeCoupling(Double coupling, int components);

    protected double getCohesionObjective() {
        if (!this.evaluated) evaluate();
        return this.cohesionObjective;
    }

    protected double getCouplingObjective() {
        if (!this.evaluated) evaluate();
        return this.couplingObjective;
    }

    protected double getComponentSizeObjective() {
        if (!this.evaluated) evaluate();
        return this.componentSizeObjective;
    }

    protected double getComponentRangeObjective() {
        if (!this.evaluated) evaluate();
        return this.componentRangeObjective;
    }

    protected double getCohesiveComponentObjective() {
        if (!this.evaluated) evaluate();
        return this.cohesiveComponentObjective;
    }

}
