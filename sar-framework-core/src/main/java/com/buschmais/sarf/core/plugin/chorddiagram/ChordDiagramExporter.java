package com.buschmais.sarf.core.plugin.chorddiagram;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.core.plugin.api.DiagramExporter;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.XOManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Lazy
public class ChordDiagramExporter implements DiagramExporter {

    private final XOManager xoManager;

    private final DiagramRepository diagramRepository;

    public ChordDiagramExporter(XOManager xoManager, DiagramRepository diagramRepository) {
        this.xoManager = xoManager;
        this.diagramRepository = diagramRepository;
    }

    @Override
    public String export(Set<ComponentDescriptor> components) {
        StringBuilder builder = new StringBuilder();
        builder.append("[\n")
            .append(components.stream()
                .map(c -> formatComponentDescriptor(c, "  "))
                .collect(Collectors.joining(",\n")))
            .append("\n]");
        return builder.toString();
    }

    private String formatComponentDescriptor(ComponentDescriptor componentDescriptor, String indentation) {
        StringBuilder format = new StringBuilder(indentation + "{\n");
        String newIndentation = indentation + "  ";
        format.append(indentation)
            .append("\"name\": \"" + componentDescriptor.getName())
            .append("\", \"description\": \"")
            .append(Arrays.toString(componentDescriptor.getTopWords()))
            .append("\"")
            .append(", \"size\": " + diagramRepository.getTypeCountRecursive(this.xoManager.getId(componentDescriptor)));

        if (componentDescriptor.getContainedComponents().size() > 0 || componentDescriptor.getContainedTypes().size() > 0) {
            format.append(",\n")
                .append(indentation)
                .append("\"children\": [\n");
            Set contained = new HashSet();
            contained.addAll(componentDescriptor.getContainedTypes());
            contained.addAll(componentDescriptor.getContainedComponents());
            format.append(contained.stream()
                .map(t -> {
                    if (t instanceof TypeDescriptor) {
                        return formatTypeDescriptor(componentDescriptor, (TypeDescriptor) t, newIndentation);
                    } else {
                        return formatComponentDescriptor((ComponentDescriptor) t, newIndentation);
                    }
                })
                .collect(Collectors.joining(",\n")));
            format.append("\n" + indentation + "]");
        }
        // component dependencies
        format.append(",\n")
            .append(indentation)
            .append("\"dependencies\": [\n")
            .append(formatComponentDependency(componentDescriptor, componentDescriptor, newIndentation));
        if (diagramRepository.getDependencies(xoManager.getId(componentDescriptor)).hasResult()) {
            format.append(",\n");
            format.append(StreamSupport.stream(diagramRepository.getDependencies(xoManager.getId(componentDescriptor)).spliterator(), false)
                .map(dep -> formatComponentDependency(componentDescriptor, dep, newIndentation))
                .collect(Collectors.joining(",\n"))
            );

        }
        if (diagramRepository.getTypeDependencies(xoManager.getId(componentDescriptor)).hasResult()) {
            format.append(",\n");
            format.append(StreamSupport.stream(diagramRepository.getTypeDependencies(xoManager.getId(componentDescriptor)).spliterator(), false)
                .map(dep -> formatTypeDependency(componentDescriptor, dep, newIndentation))
                .collect(Collectors.joining(",\n"))
            );
        }
        format.append("\n" + indentation + "]\n");
        format.append(indentation + "}");
        return format.toString();
    }

    private String formatComponentDependency(ComponentDescriptor from, ComponentDescriptor dep, String indentation) {
        StringBuilder format = new StringBuilder(indentation + "{\"name\": \"");
        format.append(dep.getName())
            .append("\", \"weight\": ")
            .append(diagramRepository.getDependencyCount(xoManager.getId(from), xoManager.getId(dep)))
            .append("}");
        return format.toString();
    }

    private String formatTypeDependency(ComponentDescriptor from, TypeDescriptor dep, String indentation) {
        StringBuilder format = new StringBuilder(indentation + "{\"name\": \"");
        format.append(dep.getName())
            .append("\", \"weight\": ")
            .append(diagramRepository.getTypeDependencyCount(xoManager.getId(from), xoManager.getId(dep)))
            .append("}");
        return format.toString();
    }

    private String formatTypeDescriptor(ComponentDescriptor parent, TypeDescriptor typeDescriptor, String indentation) {
        StringBuilder format = new StringBuilder(indentation + "{\"name\": \"");
        Result<Map> componentDeps = this.diagramRepository.getTypeComponentDependenciesIn(this.xoManager.getId(parent), this.xoManager.getId(typeDescriptor));
        Result<Map> typeDeps = this.diagramRepository.getTypeTypeDependenciesIn(this.xoManager.getId(parent), this.xoManager.getId(typeDescriptor));
        format.append(typeDescriptor.getName())
            .append("\", \"description\": \"")
            .append(typeDescriptor.getFullQualifiedName())
            .append("\", \"size\": 1");
        if (componentDeps.hasResult() || typeDeps.hasResult()) {
            format.append(",\n")
                .append(indentation)
                .append("\"dependencies\": [\n")
                .append(StreamSupport.stream(componentDeps.spliterator(), false)
                            .map(e -> "  " + indentation + "{\"name\": \"" + e.get("name") + "\",\"weight\": " + e.get("weight") + "}")
                            .collect(Collectors.joining(",\n")))
                .append(StreamSupport.stream(typeDeps.spliterator(), false)
                            .map(e -> "  " + indentation + "{\"name\": \"" + e.get("name") + "\",\"weight\": " + e.get("weight") + "}")
                            .collect(Collectors.joining(",\n")))
                .append("\n")
                .append(indentation)
                .append("]\n");
        }
            format.append(indentation).append("}");
        return format.toString();
    }

    private String formatTypeDependency(TypeDescriptor from, TypeDescriptor dep, String indentation) {
        StringBuilder format = new StringBuilder(indentation + "{\"name\": \"");
        format.append(dep.getName())
            .append("\", \"weight\": ")
            .append(this.diagramRepository.getDependencyWeight(xoManager.getId(from), xoManager.getId(dep)))
            .append("}");
        return format.toString();
    }
}
