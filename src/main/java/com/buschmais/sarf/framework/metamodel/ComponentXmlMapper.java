package com.buschmais.sarf.framework.metamodel;

import com.buschmais.sarf.framework.Materializable;
import com.buschmais.sarf.plugin.api.RuleXmlMapper;
import com.buschmais.sarf.plugin.dependency.AnnotatedByRuleXmlMapper;
import com.buschmais.sarf.plugin.dependency.DependencyRuleXmlMapper;
import com.buschmais.sarf.plugin.dependency.ExtendsRuleXmlMapper;
import com.buschmais.sarf.plugin.dependency.ImplementsRuleXmlMapper;
import com.buschmais.sarf.plugin.packagenaming.PackageNamingRuleXmlMapper;
import com.buschmais.sarf.plugin.typenaming.TypeNamingRuleXmlMapper;
import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@Materializable(ComponentDescriptor.class)
public class ComponentXmlMapper {

    @Getter
    @XmlID
    @XmlAttribute(name = "shape")
    private String shape;

    @Getter
    @XmlID
    @XmlAttribute(name = "name")
    private String name;

    @Getter
    @XmlElement(name = "Component")
    private Set<ComponentXmlMapper> containedComponents;

    @Getter
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
    Set<RuleXmlMapper> identifyingRules;


}
