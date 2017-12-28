package com.buschmais.sarf.plugin.circlepackagingdiagram;

import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.plugin.api.DiagramExporter;
import com.buschmais.xo.api.XOManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Lazy
public class CirclePackagingDiagramExporter implements DiagramExporter {

    private XOManager xoManager;

    @Autowired
    public CirclePackagingDiagramExporter(XOManager xoManager) {
        this.xoManager = xoManager;
    }

    @Override
    public String export(Set<ComponentDescriptor> components) {
        this.xoManager.currentTransaction().begin();

        this.xoManager.currentTransaction().commit();
        return "";
    }
}
