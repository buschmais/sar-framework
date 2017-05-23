package com.buchmais.sarf.classification.criterion;

import com.buchmais.sarf.classification.Materializable;
import com.buchmais.sarf.node.ClassificationCriterionDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public abstract class ClassificationCriterion implements Comparable<ClassificationCriterion>, Materializable<ClassificationCriterionDescriptor> {

    ClassificationCriterionDescriptor classificationCriterionDescriptor;

    @Getter
    @XmlAttribute(name = "weight")
    double weight;

    public ClassificationCriterion(double weight) {
        this.weight = weight;
    }

    public abstract Set<ComponentDescriptor> classify(Integer iteration);

    public ClassificationCriterionDescriptor getClassificationCriterionDescriptor() {
        if (this.classificationCriterionDescriptor == null) {
            materialize();
        }
        return this.classificationCriterionDescriptor;
    }

    @Override
    public int compareTo(ClassificationCriterion o) {
        int res = getClass().getName().compareTo(o.getClass().getName());
        //todo
        return res;
    }
}