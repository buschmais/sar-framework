package com.buschmais.sarf.plugin.api;

import com.buschmais.sarf.plugin.api.criterion.RuleBasedCriterionDescriptor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ContainedIn {
    Class<? extends RuleBasedCriterionDescriptor> value();
}
