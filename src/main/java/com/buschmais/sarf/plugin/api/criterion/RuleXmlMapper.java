package com.buschmais.sarf.plugin.api.criterion;

import javax.xml.bind.annotation.XmlAttribute;

import com.buschmais.sarf.plugin.api.XmlMapper;

/**
 * @author Stephan Pirnbaum
 */
public abstract class RuleXmlMapper implements XmlMapper {

    @XmlAttribute(name = "weight")
    public double weight;

    @XmlAttribute(name = "rule")
    public String rule;

}
