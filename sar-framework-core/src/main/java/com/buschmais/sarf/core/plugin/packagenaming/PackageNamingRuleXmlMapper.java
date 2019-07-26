package com.buschmais.sarf.core.plugin.packagenaming;

import com.buschmais.sarf.core.plugin.api.Materializable;
import com.buschmais.sarf.core.plugin.api.criterion.RuleXmlMapper;

/**
 * JAXB XML Mapper for the package naming rule.
 *
 * @author Stephan Pirnbaum
 */
@Materializable(PackageNamingRuleDescriptor.class)
public class PackageNamingRuleXmlMapper extends RuleXmlMapper {}
