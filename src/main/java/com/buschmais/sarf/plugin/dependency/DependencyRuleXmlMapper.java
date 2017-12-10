package com.buschmais.sarf.plugin.dependency;

import com.buschmais.sarf.framework.Materializable;
import com.buschmais.sarf.plugin.api.RuleXmlMapper;

/**
 * @author Stephan Pirnbaum
 */
@Materializable(DependencyRuleDescriptor.class)
public class DependencyRuleXmlMapper<T extends DependencyRuleDescriptor> extends RuleXmlMapper<T> {}
