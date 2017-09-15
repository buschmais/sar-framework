package com.buschmais.sarf;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.classification.configuration.ClassificationConfigurationDescriptor;
import com.buschmais.sarf.classification.configuration.ClassificationConfigurationRepository;
import com.buschmais.sarf.classification.criterion.ClassificationInfoDescriptor;
import com.buschmais.sarf.classification.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.sarf.classification.criterion.RuleDescriptor;
import com.buschmais.sarf.classification.criterion.cohesion.CohesionCriterionDescriptor;
import com.buschmais.sarf.classification.criterion.dependency.*;
import com.buschmais.sarf.classification.criterion.packagenaming.PackageNamingCriterionDescriptor;
import com.buschmais.sarf.classification.criterion.packagenaming.PackageNamingRepository;
import com.buschmais.sarf.classification.criterion.packagenaming.PackageNamingRuleDescriptor;
import com.buschmais.sarf.classification.criterion.typenaming.TypeNamingCriterionDescriptor;
import com.buschmais.sarf.classification.criterion.typenaming.TypeNamingRepository;
import com.buschmais.sarf.classification.criterion.typenaming.TypeNamingRuleDescriptor;
import com.buschmais.sarf.metamodel.ComponentDependsOn;
import com.buschmais.sarf.metamodel.ComponentDescriptor;
import com.buschmais.sarf.repository.ComponentRepository;
import com.buschmais.sarf.repository.MetricRepository;
import com.buschmais.sarf.repository.TypeRepository;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Properties;

/**
 * @author Stephan Pirnbaum
 */
public class DatabaseHelper {

    private static final Logger LOG = LogManager.getLogger(DatabaseHelper.class);

    public static XOManager xoManager;

    public static void setUpDB(URI storeUri) {
        LOG.info("Setting up Database");
        Properties p = new Properties();
        p.put("neo4j.dbms.allow_format_migration", "true");
        XOUnit xoUnit = XOUnit.builder()
                .properties(p)
                .provider(EmbeddedNeo4jXOProvider.class)
                .type(TypeDescriptor.class)
                .type(TypeDependsOnDescriptor.class)
                .type(TypeRepository.class)
                .type(PackageNamingRuleDescriptor.class)
                .type(PackageNamingCriterionDescriptor.class)
                .type(ComponentRepository.class)
                .type(ComponentDescriptor.class)
                .type(ComponentDependsOn.class)
                .type(ClassificationConfigurationDescriptor.class)
                .type(ClassificationInfoDescriptor.class)
                .type(ClassificationConfigurationRepository.class)
                .type(TypeNamingCriterionDescriptor.class)
                .type(DependencyCriterionDescriptor.class)
                .type(DependencyDescriptor.class)
                .type(MetricRepository.class)
                .type(RuleBasedCriterionDescriptor.class)
                .type(RuleDescriptor.class)
                .type(CohesionCriterionDescriptor.class)
                .type(TypeNamingRuleDescriptor.class)
                .type(TypeNamingRepository.class)
                .type(PackageNamingRepository.class)
                .type(DependencyRepository.class)
                .type(AnnotatedByDescriptor.class)
                .type(ExtendsDescriptor.class)
                .type(ImplementsDescriptor.class)
                .uri(storeUri)
                .build();
        XOManagerFactory factory = XO.createXOManagerFactory(xoUnit);
        LOG.info("Setting up Database Successful");
        DatabaseHelper.xoManager = factory.createXOManager();
    }

    public static void stopDB() {
        DatabaseHelper.xoManager.close();
    }
}
