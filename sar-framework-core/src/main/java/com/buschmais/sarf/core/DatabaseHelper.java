package com.buschmais.sarf.core;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.framework.configuration.ClassificationConfigurationDescriptor;
import com.buschmais.sarf.core.framework.configuration.ClassificationConfigurationRepository;
import com.buschmais.sarf.core.framework.metamodel.ComponentDependsOn;
import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.core.framework.repository.ComponentRepository;
import com.buschmais.sarf.core.framework.repository.MetricRepository;
import com.buschmais.sarf.core.framework.repository.TypeRepository;
import com.buschmais.sarf.core.plugin.api.ClassificationInfoDescriptor;
import com.buschmais.sarf.core.plugin.api.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.sarf.core.plugin.api.criterion.RuleDescriptor;
import com.buschmais.sarf.core.plugin.chorddiagram.DiagramRepository;
import com.buschmais.sarf.core.plugin.cohesion.CohesionCriterionDescriptor;
import com.buschmais.sarf.core.plugin.dependency.*;
import com.buschmais.sarf.core.plugin.packagenaming.PackageNamingCriterionDescriptor;
import com.buschmais.sarf.core.plugin.packagenaming.PackageNamingRepository;
import com.buschmais.sarf.core.plugin.packagenaming.PackageNamingRuleDescriptor;
import com.buschmais.sarf.core.plugin.typenaming.TypeNamingCriterionDescriptor;
import com.buschmais.sarf.core.plugin.typenaming.TypeNamingRepository;
import com.buschmais.sarf.core.plugin.typenaming.TypeNamingRuleDescriptor;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.net.URI;
import java.util.Properties;

/**
 * @author Stephan Pirnbaum
 */
@Configuration
@Slf4j
public class DatabaseHelper {

    @Bean
    @Lazy
    public XOManager xOManager(URI storeUri) {
        LOGGER.info("Setting up Database");
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
            .type(DiagramRepository.class)
            .uri(storeUri)
            .build();
        XOManagerFactory factory = XO.createXOManagerFactory(xoUnit);
        LOGGER.info("Setting up Database Successful");
        return factory.createXOManager();
    }

    @Bean
    @Lazy
    public MetricRepository metricRepository(XOManager xoManager) {
        return xoManager.getRepository(MetricRepository.class);
    }

    @Bean
    @Lazy
    public TypeRepository typeRepository(XOManager xoManager) {
        return xoManager.getRepository(TypeRepository.class);
    }

    @Bean
    @Lazy
    public ComponentRepository componentRepository(XOManager xoManager) {
        return xoManager.getRepository(ComponentRepository.class);
    }

    @Bean
    @Lazy
    public TypeNamingRepository typeNamingRepository(XOManager xoManager) {
        return xoManager.getRepository(TypeNamingRepository.class);
    }

    @Bean
    @Lazy
    public PackageNamingRepository packageNamingRepository(XOManager xoManager) {
        return xoManager.getRepository(PackageNamingRepository.class);
    }

    @Bean
    @Lazy
    public DependencyRepository dependencyRepository(XOManager xoManager) {
        return xoManager.getRepository(DependencyRepository.class);
    }

    @Bean
    @Lazy
    public DiagramRepository diagramRepository(XOManager xoManager) {
        return xoManager.getRepository(DiagramRepository.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
