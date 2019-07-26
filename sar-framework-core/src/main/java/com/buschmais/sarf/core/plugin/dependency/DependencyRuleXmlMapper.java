package com.buschmais.sarf.core.plugin.dependency;

import com.buschmais.sarf.core.plugin.api.Materializable;
import com.buschmais.sarf.core.plugin.api.criterion.RuleXmlMapper;

/**
 * JAXB XML Mapper for the dependency rule.
 *
 * @author Stephan Pirnbaum
 */
@Materializable(DependencyRuleDescriptor.class)
public class DependencyRuleXmlMapper extends RuleXmlMapper {}
