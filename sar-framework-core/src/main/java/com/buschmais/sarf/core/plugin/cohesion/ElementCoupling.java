package com.buschmais.sarf.core.plugin.cohesion;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/** Class representing the coupling structure between elements which refer to either {@link TypeDescriptor},
 * {@link ComponentDescriptor}, or a combination of them.
 *
 * @author stephan.pirnbaum
 */
@ToString
public class ElementCoupling implements Comparable<ElementCoupling> {

    @Getter private final long source;
    @Getter private double coupling = 0;
    @Getter private final long target;

    private final int hash;

    public ElementCoupling(long source, long target) {
        this.source = source;
        this.target = target;
        this.hash = Objects.hash(source, target);
    }

    public ElementCoupling(long source, double coupling, long target) {
        this(source, target);
        this.coupling = coupling;
    }

    /**
     * Adds the specified coupling value.
     *
     * @param coupling The coupling value to add.
     */
    public void addCoupling(double coupling) {
        this.coupling += coupling;
    }

    /**
     * Normalizes the coupling with the given denominator.
     *
     * @param denominator The denominator.
     */
    public void normalizeCoupling(int denominator) {
        this.coupling /= denominator;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementCoupling that = (ElementCoupling) o;
        return source == that.source &&
            target == that.target;
    }

    @Override
    public int compareTo(ElementCoupling o) {
        if (this.source == o.source) {
            return Long.compare(this.target, o.target);
        }
        return Long.compare(this.source, o.source);
    }
}
