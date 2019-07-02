package com.buschmais.sarf.plugin.circlepackagingdiagram;

import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.plugin.api.DiagramExporter;
import com.buschmais.xo.api.XOManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Lazy
@RequiredArgsConstructor
public class CirclePackagingDiagramExporter implements DiagramExporter {

    private final XOManager xoManager;

    @Override
    public String export(Set<ComponentDescriptor> components) {
        this.xoManager.currentTransaction().begin();

        this.xoManager.currentTransaction().commit();
        return "";
    }
}
