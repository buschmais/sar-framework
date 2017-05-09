package com.buchmais.sarf.classification;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.node.PatternDescriptor;
import com.buschmais.xo.api.XOManager;
import lombok.Getter;

/**
 * @author Stephan Pirnbaum
 */
public class Pattern implements Comparable<Pattern> {

    @Getter
    private String shape;

    @Getter
    private String name;

    @Getter
    private String regEx;

    private PatternDescriptor patternDescriptor;

    public Pattern(String shape, String name, String regEx) {
        this.shape = shape;
        this.name = name;
        this.regEx = regEx;
    }

    public static Pattern of(PatternDescriptor patternDescriptor) {
        Pattern pattern = new Pattern(patternDescriptor.getShape(), patternDescriptor.getName(), patternDescriptor.getRegEx());
        pattern.patternDescriptor = patternDescriptor;
        return pattern;
    }

    public PatternDescriptor materialize() {
        SARFRunner.xoManager.currentTransaction().begin();
        PatternDescriptor patternDescriptor = SARFRunner.xoManager.create(PatternDescriptor.class);
        patternDescriptor.setShape(this.shape);
        patternDescriptor.setName(this.name);
        patternDescriptor.setRegEx(this.regEx);
        SARFRunner.xoManager.currentTransaction().commit();
        this.patternDescriptor = patternDescriptor;
        return patternDescriptor;
    }

    public PatternDescriptor getPatternDescriptor() {
        if (this.patternDescriptor == null) {
            materialize();
        }
        return this.patternDescriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pattern pattern = (Pattern) o;

        if (shape != null ? !shape.equals(pattern.shape) : pattern.shape != null) return false;
        if (name != null ? !name.equals(pattern.name) : pattern.name != null) return false;
        return regEx != null ? regEx.equals(pattern.regEx) : pattern.regEx == null;
    }

    @Override
    public int hashCode() {
        int result = shape != null ? shape.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (regEx != null ? regEx.hashCode() : 0);
        return result;
    }

    public int compareTo(Pattern o) {
        return 0;
    }
}
