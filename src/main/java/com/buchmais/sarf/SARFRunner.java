package com.buchmais.sarf;

import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by steph on 04.05.2017.
 */
public class SARFRunner {

    public static void main(String[] args) throws URISyntaxException {
        Properties p = new Properties();
        p.put("neo4j.dbms.allow_format_migration", "true");
        XOUnit xoUnit = XOUnit.builder()
                .properties(p)
                .provider(EmbeddedNeo4jXOProvider.class)
                .type(TypeDescriptor.class)
                .type(TypeDependsOnDescriptor.class)
                .type(TypeRepository.class)
                .uri(new URI("file:///E:/Development/trainingszeitverwaltung-kraftraum/target/jqassistant/store"))
                .build();
        XOManagerFactory factory = XO.createXOManagerFactory(xoUnit);
        XOManager xoManager = factory.createXOManager();
        xoManager.currentTransaction().begin();
        TypeRepository typeRepository = xoManager.getRepository(TypeRepository.class);
        for (TypeDescriptor type : typeRepository.getAllInternalTypes()) {
            System.out.println(type.getFullQualifiedName());
        }
        xoManager.currentTransaction().commit();
        xoManager.close();
        factory.close();
    }
}
