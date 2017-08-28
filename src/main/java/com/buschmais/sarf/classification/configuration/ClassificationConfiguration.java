package com.buschmais.sarf.classification.configuration;

import com.buschmais.sarf.Materializable;
import com.buschmais.sarf.SARFRunner;
import com.buschmais.sarf.classification.criterion.ClassificationCriterion;
import com.buschmais.sarf.classification.criterion.ClassificationCriterionDescriptor;
import com.buschmais.sarf.metamodel.Component;
import com.buschmais.sarf.metamodel.ComponentDescriptor;
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
@XmlAccessorType(XmlAccessType.FIELD)
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

    @XmlType
    @XmlEnum(String.class)
    public enum Optimization {
        @XmlEnumValue("similarity")
        SIMILARITY,
        @XmlEnumValue("coupling")
        COUPLING
    }

    @Getter
    @XmlAttribute(name = "iteration")
    Integer iteration;

    @Getter
    @XmlAttribute(name = "basePackage")
    String basePackage;

    @Getter
    @XmlAttribute(name = "typeName")
    String typeName;

    @Getter
    @XmlAttribute(name = "artifact")
    String artifact;

    @XmlAttribute(name = "decomposition")
    private Decomposition decomposition;

    @XmlAttribute(name = "optimization")
    private Optimization optimization;

    @Getter
    @XmlElement(name = "Component")
    Set<Component> model = new HashSet<>();

    @Getter
    Set<ClassificationCriterion> classificationCriteria;

    @XmlTransient
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

    void setDecomposition(Decomposition decomposition) {
        this.decomposition = decomposition;
    }

    public Optimization getOptimization() {
        if (this.optimization == null) {
            return Optimization.SIMILARITY;
        } else {
            return this.optimization;
        }
    }

    public void setOptimization(Optimization optimization) {
        this.optimization = optimization;
    }

    public ClassificationConfigurationDescriptor materialize() {
        LOG.info("Materializing Configuration to Database");
        Set<ClassificationCriterionDescriptor> descriptors = this.classificationCriteria.stream().map(ClassificationCriterion::materialize).collect(Collectors.toSet());
        Set<ComponentDescriptor> componentDescriptors = this.model.stream().map(Component::materialize).collect(Collectors.toSet());
        SARFRunner.xoManager.currentTransaction().begin();
        if (this.classificationConfigurationDescriptor == null) {
            this.classificationConfigurationDescriptor = SARFRunner.xoManager.create(ClassificationConfigurationDescriptor.class);
            this.classificationConfigurationDescriptor.setIteration(this.iteration);
            this.classificationConfigurationDescriptor.setDecomposition(this.getDecomposition().toString());
            this.classificationConfigurationDescriptor.setOptimization(this.getOptimization().toString());
            this.classificationConfigurationDescriptor.getClassificationCriteria().addAll(descriptors);
            this.classificationConfigurationDescriptor.getDefinedComponents().addAll(componentDescriptors);
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return this.classificationConfigurationDescriptor;
    }

    public abstract ClassificationConfiguration of(ClassificationConfigurationDescriptor classificationConfigurationDescriptor);
}
