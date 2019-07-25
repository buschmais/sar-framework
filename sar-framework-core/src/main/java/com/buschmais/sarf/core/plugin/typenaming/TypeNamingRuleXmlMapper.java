package com.buschmais.sarf.core.plugin.typenaming;

import com.buschmais.sarf.core.plugin.api.Materializable;
import com.buschmais.sarf.core.plugin.api.criterion.RuleXmlMapper;

/**
 * JAXB XML Mapper for the type naming rule.
 *
 * @author Stephan Pirnbaum
 */
@Materializable(TypeNamingRuleDescriptor.class)
public class TypeNamingRuleXmlMapper extends RuleXmlMapper {}
