package com.buschmais.sarf.framework.metamodel;

import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;

import com.buschmais.sarf.plugin.api.Materializable;
import com.buschmais.sarf.plugin.api.XmlMapper;
import com.buschmais.sarf.plugin.api.criterion.RuleXmlMapper;
import com.buschmais.sarf.plugin.dependency.AnnotatedByRuleXmlMapper;
import com.buschmais.sarf.plugin.dependency.DependencyRuleXmlMapper;
import com.buschmais.sarf.plugin.dependency.ExtendsRuleXmlMapper;
import com.buschmais.sarf.plugin.dependency.ImplementsRuleXmlMapper;
import com.buschmais.sarf.plugin.packagenaming.PackageNamingRuleXmlMapper;
import com.buschmais.sarf.plugin.typenaming.TypeNamingRuleXmlMapper;

/**
 * @author Stephan Pirnbaum
 */
@Materializable(ComponentDescriptor.class)
public class ComponentXmlMapper implements XmlMapper {

    @XmlID
    @XmlAttribute(name = "shape")
    public String shape;

    @XmlID
    @XmlAttribute(name = "name")
    public String name;

    @XmlElement(name = "Component")
    public Set<ComponentXmlMapper> containedComponents;

    @XmlElementWrapper(name = "Rules")
    @XmlElements(
            {
                    @XmlElement(name = "Name", type = TypeNamingRuleXmlMapper.class),
                    @XmlElement(name = "Package", type = PackageNamingRuleXmlMapper.class),
                    @XmlElement(name = "Dependency", type = DependencyRuleXmlMapper.class),
                    @XmlElement(name = "AnnotatedBy", type = AnnotatedByRuleXmlMapper.class),
                    @XmlElement(name = "Extends", type = ExtendsRuleXmlMapper.class),
                    @XmlElement(name = "Implements", type = ImplementsRuleXmlMapper.class)
            }
    )
    public Set<RuleXmlMapper> identifyingRules;

}
