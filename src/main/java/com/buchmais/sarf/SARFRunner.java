package com.buchmais.sarf;

import com.buchmais.sarf.classification.*;
import com.buchmais.sarf.node.*;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by steph on 04.05.2017.
 */
public class SARFRunner {

    public static XOManager xoManager;

    private static ActiveClassificationConfiguration activeClassificationConfiguration;

    private static ConfigurationHistory configurationHistory;

    public static void main(String[] args) throws URISyntaxException {
        Properties p = new Properties();
        p.put("neo4j.dbms.allow_format_migration", "true");
        XOUnit xoUnit = XOUnit.builder()
                .properties(p)
                .provider(EmbeddedNeo4jXOProvider.class)
                .type(TypeDescriptor.class)
                .type(TypeDependsOnDescriptor.class)
                .type(TypeRepository.class)
                .type(PatternDescriptor.class)
                .type(PackageNamingCriterionDescriptor.class)
                .type(ComponentRepository.class)
                .type(ComponentDescriptor.class)
                .type(ComponentDependsOn.class)
                .type(ClassificationConfigurationDescriptor.class)
                .type(ClassificationInfoDescriptor.class)
                .uri(new URI("file:///E:/Development/trainingszeitverwaltung-kraftraum/target/jqassistant/store"))
                .build();
        XOManagerFactory factory = XO.createXOManagerFactory(xoUnit);
        xoManager = factory.createXOManager();
        xoManager.currentTransaction().begin();
        xoManager.getRepository(TypeRepository.class).markAllInternalTypes("de.htw");
        xoManager.currentTransaction().commit();
        startIteration();
        factory.close();

    }

    public static ActiveClassificationConfiguration startIteration() {
        // store old iteration
        Integer iteration = 1;
        if (activeClassificationConfiguration != null) {
            iteration = activeClassificationConfiguration.getIteration() + 1;
            configurationHistory.addIteration(activeClassificationConfiguration.prepareNextIteration());
        } else {
            activeClassificationConfiguration = ActiveClassificationConfiguration.getInstance();
        }
        activeClassificationConfiguration.addClassificationCriterion(createPNC());
        activeClassificationConfiguration.materialize();
        activeClassificationConfiguration.execute();
        return null;
    }

    public static PackageNamingCriterion createPNC() {
        PackageNamingCriterion packageNamingCriterion = new PackageNamingCriterion(1);
        Pattern pattern = new Pattern(
                "Module",
                "User",
                1,
                "de\\.htw\\.kraftraum\\.trainingszeitverwaltung\\.user\\..*"
        );
        pattern.materialize();
        packageNamingCriterion.addRule(pattern);
        packageNamingCriterion.materialize();
        return packageNamingCriterion;
    }
}
