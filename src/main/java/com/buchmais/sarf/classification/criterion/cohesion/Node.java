package com.buchmais.sarf.classification.criterion.cohesion;

import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stephan Pirnbaum
 */
public class Node implements Comparable<Node> {

    @Getter
    private Long id;

    private Map<Node, Double> similarities;

    private Map<Node, Double> couplings;

    public Node(Long id) {
        this.id = id;
        this.similarities = new HashMap<>();
        this.couplings = new HashMap<>();
    }

    public Node(Long id, Map<Node, Double> similarities, Map<Node, Double> couplings) {
        this.id = id;
        this.similarities = similarities;
        this.couplings = couplings;
    }

    public void addSimilarity(Node node, Double similarity) {
        this.similarities.put(node, similarity);
    }

    public void addCoupling(Node node, Double coupling) {
        this.couplings.put(node, coupling);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(Node o) {
        return this.id.compareTo(o.id);
    }

    public Double computeCouplingTo(Collection<Long> to) {
        return this.couplings.entrySet()
                .stream()
                .filter(e -> to.contains(e.getKey().getId()))
                .mapToDouble(Map.Entry::getValue).sum();
    }

    public Double computeSimilarityTo(Collection<Long> to) {
        return this.similarities.entrySet()
                .stream()
                .filter(e -> to.contains(e.getKey().getId()))
                .mapToDouble(Map.Entry::getValue).sum();
    }
}
