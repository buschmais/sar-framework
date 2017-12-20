package com.buschmais.sarf;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.framework.configuration.ClassificationConfigurationDescriptor;
import com.buschmais.sarf.framework.configuration.ClassificationConfigurationRepository;
import com.buschmais.sarf.framework.metamodel.ComponentDependsOn;
import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.framework.repository.ComponentRepository;
import com.buschmais.sarf.framework.repository.MetricRepository;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.sarf.plugin.api.ClassificationInfoDescriptor;
import com.buschmais.sarf.plugin.api.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.sarf.plugin.api.criterion.RuleDescriptor;
import com.buschmais.sarf.plugin.cohesion.CohesionCriterionDescriptor;
import com.buschmais.sarf.plugin.dependency.*;
import com.buschmais.sarf.plugin.packagenaming.PackageNamingCriterionDescriptor;
import com.buschmais.sarf.plugin.packagenaming.PackageNamingRepository;
import com.buschmais.sarf.plugin.packagenaming.PackageNamingRuleDescriptor;
import com.buschmais.sarf.plugin.typenaming.TypeNamingCriterionDescriptor;
import com.buschmais.sarf.plugin.typenaming.TypeNamingRepository;
import com.buschmais.sarf.plugin.typenaming.TypeNamingRuleDescriptor;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.net.URI;
import java.util.Properties;

/**
 * @author Stephan Pirnbaum
 */
@Configuration
public class DatabaseHelper {

    private static final Logger LOG = LogManager.getLogger(DatabaseHelper.class);

    public static XOManager xoManager = null;

    @Bean
    @Lazy
    public XOManager xOManager(URI storeUri) {
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
            .type(DependencyRuleDescriptor.class)
            .type(MetricRepository.class)
            .type(RuleBasedCriterionDescriptor.class)
            .type(RuleDescriptor.class)
            .type(CohesionCriterionDescriptor.class)
            .type(TypeNamingRuleDescriptor.class)
            .type(TypeNamingRepository.class)
            .type(PackageNamingRepository.class)
            .type(DependencyRepository.class)
            .type(AnnotatedByRuleDescriptor.class)
            .type(ExtendsRuleDescriptor.class)
            .type(ImplementsRuleDescriptor.class)
            .uri(storeUri)
            .build();
        XOManagerFactory factory = XO.createXOManagerFactory(xoUnit);
        LOG.info("Setting up Database Successful");
        DatabaseHelper.xoManager = factory.createXOManager();
        return DatabaseHelper.xoManager;
    }

}
