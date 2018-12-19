package com.buschmais.sarf.framework.configuration;

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.buschmais.sarf.framework.metamodel.ComponentXmlMapper;
import com.buschmais.sarf.plugin.api.Materializable;
import com.buschmais.sarf.plugin.api.XmlMapper;

/**
 * @author Stephan Pirnbaum
 */
@XmlRootElement(name = "Configuration")
@Materializable(ClassificationConfigurationDescriptor.class)
public class ClassificationConfigurationXmlMapper implements XmlMapper{

    @XmlAttribute(name = "iteration")
    public Integer iteration;

    @XmlAttribute(name = "basePackage")
    public String basePackage;

    @XmlAttribute(name = "typeName")
    public String typeName;

    @XmlAttribute(name = "artifact")
    public String artifact;

    @XmlAttribute(name = "generations")
    public Integer generations;

    @XmlAttribute(name = "populationSize")
    public Integer populationSize;

    @XmlAttribute(name = "decomposition")
    public Decomposition decomposition;

    @XmlAttribute(name = "optimization")
    public Optimization optimization;

    @XmlElement(name = "Component")
    public Set<ComponentXmlMapper> definedComponents = new HashSet<>();

}
