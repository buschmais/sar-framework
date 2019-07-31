package com.buschmais.sarf.core.plugin.cohesion;

import com.buschmais.sarf.core.framework.configuration.Decomposition;
import com.buschmais.sarf.core.framework.configuration.Optimization;
import com.buschmais.sarf.core.plugin.api.Materializable;
import com.buschmais.sarf.core.plugin.api.XmlMapper;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * JAXB XML Mapper for the evolution.
 *
 * @author Stephan Pirnbaum
 */
@Materializable(CohesionCriterionDescriptor.class)
public class EvolutionXmlMapper implements XmlMapper {

    @XmlAttribute(name = "generations")
    public Integer generations;

    @XmlAttribute(name = "populationSize")
    public Integer populationSize;

    @XmlAttribute(name = "decomposition")
    public Decomposition decomposition;

    @XmlAttribute(name = "optimization")
    public Optimization optimization;

}
