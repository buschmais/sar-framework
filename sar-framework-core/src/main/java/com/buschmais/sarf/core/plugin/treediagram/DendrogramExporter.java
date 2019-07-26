package com.buschmais.sarf.core.plugin.treediagram;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.core.plugin.api.DiagramExporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Exports component data for visualizing it with a dendrogram.
 * A dendrogram is a type of diagram which operates on a tree-like structure.
 * The exporter fuels two visualizations: an interactive, collapsible tree and a radial tree.
 */
@Service
@Lazy
@RequiredArgsConstructor
public class DendrogramExporter implements DiagramExporter {

    private final ObjectMapper objectMapper;

    @Override
    public String export(Set<ComponentDescriptor> components) {
        List<DendrogramElement> data =
            components.stream().map(this::formatComponentDescriptor).collect(Collectors.toList());

        try {
            if (data.size() == 1) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data.get(0));
            } else {
                DendrogramElement rootComponent = new DendrogramElement();
                rootComponent.label = "root";
                rootComponent.children.addAll(data);
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootComponent);
            }
        } catch (JsonProcessingException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
    }

    private DendrogramElement formatComponentDescriptor(ComponentDescriptor componentDescriptor) {
        DendrogramElement element = new DendrogramElement();
        element.label = componentDescriptor.getName();

        if (componentDescriptor.getContainedComponents().size() > 0
            || componentDescriptor.getContainedTypes().size() > 0) {
            Set<Object> contained = new HashSet<>();
            contained.addAll(componentDescriptor.getContainedTypes());
            contained.addAll(componentDescriptor.getContainedComponents());
            element.children.addAll(contained.stream().map(t -> {
                if (t instanceof TypeDescriptor) {
                    return formatTypeDescriptor((TypeDescriptor) t);
                } else {
                    return formatComponentDescriptor((ComponentDescriptor) t);
                }
            }).collect(Collectors.toList()));
        }

        return element;
    }

    private DendrogramElement formatTypeDescriptor(TypeDescriptor typeDescriptor) {
        DendrogramElement element = new DendrogramElement();
        element.label = typeDescriptor.getName();
        return element;
    }

}
