package com.buschmais.sarf.plugin.api;

import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;

import java.util.Set;

public interface DiagramExporter {

    String export(Set<ComponentDescriptor> components);

}
