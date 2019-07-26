package com.buschmais.sarf.core.plugin.api;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExecutedBy {
    Class<? extends Executor> value();
}
