package com.buschmais.sarf.framework.configuration;

import com.buschmais.sarf.framework.metamodel.ComponentXmlMapper;
import com.buschmais.sarf.plugin.api.Materializable;
import com.buschmais.sarf.plugin.api.XmlMapper;
import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@XmlRootElement(name = "Configuration")
@Materializable(ClassificationConfigurationDescriptor.class)
public class ClassificationConfigurationXmlMapper implements XmlMapper{

    @XmlType
    @XmlEnum
    public enum Decomposition {
        @XmlEnumValue("flat")
        FLAT,
        @XmlEnumValue("deep")
        DEEP;
    }

    @XmlType
    @XmlEnum
    public enum Optimization {
        @XmlEnumValue("similarity")
        SIMILARITY,
        @XmlEnumValue("coupling")
        COUPLING
    }

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
    Set<ComponentXmlMapper> definedComponents = new HashSet<>();

}
