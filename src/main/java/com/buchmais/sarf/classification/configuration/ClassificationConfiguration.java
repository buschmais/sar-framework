package com.buchmais.sarf.classification.configuration;

import com.buchmais.sarf.Materializable;
import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.ClassificationCriterion;
import com.buchmais.sarf.classification.criterion.ClassificationCriterionDescriptor;
import com.buchmais.sarf.metamodel.Component;
import com.buchmais.sarf.metamodel.ComponentDescriptor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public abstract class ClassificationConfiguration implements Materializable<ClassificationConfigurationDescriptor> {

    private static final Logger LOG = LogManager.getLogger(ClassificationConfiguration.class);

    @XmlType
    @XmlEnum(String.class)
    public enum Decomposition {
        @XmlEnumValue("flat")
        FLAT,
        @XmlEnumValue("deep")
        DEEP
    }

    @Getter
    @XmlAttribute(name = "iteration")
    Integer iteration;

    @XmlAttribute(name = "decomposition")
    private Decomposition decomposition;

    @Getter
    @XmlElement(name = "Component")
    Set<Component> model = new HashSet<>();

    @Getter
    Set<ClassificationCriterion> classificationCriteria;

    private ClassificationConfigurationDescriptor classificationConfigurationDescriptor;

    ClassificationConfiguration(Integer iteration) {
        this.iteration = iteration;
        this.classificationCriteria = new TreeSet<>();
    }

    public boolean addClassificationCriterion(ClassificationCriterion classificationCriterion) {
        return this.classificationCriteria.add(classificationCriterion);
    }

    public boolean addAllClassificationCriterion(Set<ClassificationCriterion> classificationCriteria) {
        return this.classificationCriteria.addAll(classificationCriteria);
    }

    public Decomposition getDecomposition() {
        if (this.decomposition == null) {
            return Decomposition.DEEP;
        } else {
            return this.decomposition;
        }
    }

    public ClassificationConfigurationDescriptor materialize() {
        LOG.info("Materializing Configuration to Database");
        Set<ClassificationCriterionDescriptor> descriptors = this.classificationCriteria.stream().map(ClassificationCriterion::materialize).collect(Collectors.toSet());
        Set<ComponentDescriptor> componentDescriptors = this.model.stream().map(Component::materialize).collect(Collectors.toSet());
        SARFRunner.xoManager.currentTransaction().begin();
        if (this.classificationConfigurationDescriptor == null) {
            this.classificationConfigurationDescriptor = SARFRunner.xoManager.create(ClassificationConfigurationDescriptor.class);
            this.classificationConfigurationDescriptor.setIteration(this.iteration);
            this.classificationConfigurationDescriptor.getClassificationCriteria().addAll(descriptors);
            this.classificationConfigurationDescriptor.getDefinedComponents().addAll(componentDescriptors);
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return this.classificationConfigurationDescriptor;
    }

    public abstract ClassificationConfiguration of(ClassificationConfigurationDescriptor classificationConfigurationDescriptor);
}
