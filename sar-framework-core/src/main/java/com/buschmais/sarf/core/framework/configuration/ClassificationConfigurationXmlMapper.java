package com.buschmais.sarf.core.framework.configuration;

import com.buschmais.sarf.core.framework.metamodel.ComponentXmlMapper;
import com.buschmais.sarf.core.plugin.api.Materializable;
import com.buschmais.sarf.core.plugin.api.XmlMapper;
import com.buschmais.sarf.core.plugin.cohesion.EvolutionXmlMapper;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

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

    @XmlElement(name = "Evolution")
    public EvolutionXmlMapper evolution;

    @XmlElement(name = "Component")
    public Set<ComponentXmlMapper> definedComponents = new HashSet<>();

}
