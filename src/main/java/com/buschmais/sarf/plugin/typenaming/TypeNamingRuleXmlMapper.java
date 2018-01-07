package com.buschmais.sarf.plugin.typenaming;

import com.buschmais.sarf.plugin.api.Materializable;
import com.buschmais.sarf.plugin.api.criterion.RuleXmlMapper;

/**
 * JAXB XML Mapper for the type naming rule.
 *
 * @author Stephan Pirnbaum
 */
@Materializable(TypeNamingRuleDescriptor.class)
public class TypeNamingRuleXmlMapper extends RuleXmlMapper {}
