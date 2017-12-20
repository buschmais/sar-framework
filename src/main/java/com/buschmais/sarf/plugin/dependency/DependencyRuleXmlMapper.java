package com.buschmais.sarf.plugin.dependency;

import com.buschmais.sarf.plugin.api.Materializable;
import com.buschmais.sarf.plugin.api.criterion.RuleXmlMapper;

/**
 * JAXB XML Mapper for the dependency rule.
 *
 * @author Stephan Pirnbaum
 */
@Materializable(DependencyRuleDescriptor.class)
public class DependencyRuleXmlMapper extends RuleXmlMapper {}
