package com.buschmais.sarf.classification.configuration;

import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.Materializable;
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

    @XmlAttribute(name = "typeName")
    String typeName;

    @XmlAttribute(name = "artifact")
    String artifact;

    @XmlAttribute(name = "generations")
    Integer generations;

    @XmlAttribute(name = "populationSize")
    Integer populationSize;

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

    ClassificationConfiguration(Integer iteration, String artifact, String basePackage, String typeName,
                                Integer generations, Integer populationSize, Decomposition decomposition, Optimization optimization) {

        this.iteration = iteration;
        this.artifact = artifact;
        this.basePackage = basePackage;
        this.typeName = typeName;
        this.generations = generations;
        this.populationSize = populationSize;
        this.decomposition = decomposition;
        this.optimization = optimization;
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

    public String getTypeName() {
        if (this.typeName == null) {
            return ".*";
        } else {
            return this.typeName;
        }
    }

    public String getArtifact() {
        if (this.artifact == null) {
            return ".*";
        } else {
            return this.artifact;
        }
    }

    public Integer getGenerations() {
        if (this.generations == null) {
            return 300;
        } else {
            return this.generations;
        }
    }

    public Integer getPopulationSize() {
        return this.populationSize == null ? 100 : this.populationSize;
    }

    public void setOptimization(Optimization optimization) {
        this.optimization = optimization;
    }

    public ClassificationConfigurationDescriptor materialize() {
        LOG.info("Materializing Configuration to Database");
        Set<ClassificationCriterionDescriptor> descriptors = this.classificationCriteria.stream().map(ClassificationCriterion::materialize).collect(Collectors.toSet());
        Set<ComponentDescriptor> componentDescriptors = this.model.stream().map(Component::materialize).collect(Collectors.toSet());
        DatabaseHelper.xoManager.currentTransaction().begin();
        if (this.classificationConfigurationDescriptor == null) {
            this.classificationConfigurationDescriptor = DatabaseHelper.xoManager.create(ClassificationConfigurationDescriptor.class);
            this.classificationConfigurationDescriptor.setIteration(this.iteration);
            this.classificationConfigurationDescriptor.setDecomposition(this.getDecomposition().toString());
            this.classificationConfigurationDescriptor.setOptimization(this.getOptimization().toString());
            this.classificationConfigurationDescriptor.getClassificationCriteria().addAll(descriptors);
            this.classificationConfigurationDescriptor.getDefinedComponents().addAll(componentDescriptors);
        }
        DatabaseHelper.xoManager.currentTransaction().commit();
        return this.classificationConfigurationDescriptor;
    }

    public abstract ClassificationConfiguration of(ClassificationConfigurationDescriptor classificationConfigurationDescriptor);
}
