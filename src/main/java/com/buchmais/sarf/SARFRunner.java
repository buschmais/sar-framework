package com.buchmais.sarf;

import com.buchmais.sarf.classification.configuration.ActiveClassificationConfiguration;
import com.buchmais.sarf.classification.configuration.ConfigurationHistory;
import com.buchmais.sarf.classification.configuration.TypeCouplingEnricher;
import com.buchmais.sarf.classification.criterion.cohesion.CohesionCriterion;
import com.buchmais.sarf.node.*;
import com.buchmais.sarf.repository.ClassificationConfigurationRepository;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buchmais.sarf.repository.MetricRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by steph on 04.05.2017.
 */
public class SARFRunner {

    private static final Logger LOG = LogManager.getLogger(SARFRunner.class);

    public static XOManager xoManager;

    private static ActiveClassificationConfiguration activeClassificationConfiguration;

    private static ConfigurationHistory configurationHistory;

    public static void main(String[] args) throws URISyntaxException {
        readConfiguration();
        XOManagerFactory factory = setUpDB();
        SARFRunner.activeClassificationConfiguration.execute();
        factory.close();
    }

    public static XOManagerFactory setUpDB() throws URISyntaxException {
        LOG.info("Setting up Database");
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
                .type(ClassificationConfigurationRepository.class)
                .type(ClassNamingCriterionDescriptor.class)
                .type(DependencyCriterionDescriptor.class)
                .type(DependencyDescriptor.class)
                .type(MetricRepository.class)
                .type(RuleBasedCriterionDescriptor.class)
                .type(RuleDescriptor.class)
                .type(CohesionCriterionDescriptor.class)
                .uri(new URI("file:///E:/Development/trainingszeitverwaltung-kraftraum/target/jqassistant/store"))
                .build();
        XOManagerFactory factory = XO.createXOManagerFactory(xoUnit);
        xoManager = factory.createXOManager();
        ClassificationConfigurationRepository classificationConfigurationRepository = SARFRunner.xoManager.getRepository(ClassificationConfigurationRepository.class);
        if (SARFRunner.activeClassificationConfiguration.getIteration() == 1) {
            LOG.info("Resetting Data");
            SARFRunner.xoManager.currentTransaction().begin();
            SARFRunner.xoManager.createQuery(
                    "MATCH (sarf:SARF) DETACH DELETE sarf"
            ).execute();
            SARFRunner.xoManager.createQuery(
                    "MATCH (:Type)-[c:COUPLES]-(:Type) DELETE c"
            ).execute();
            SARFRunner.xoManager.currentTransaction().commit();
        } else if (SARFRunner.activeClassificationConfiguration.getIteration() <= classificationConfigurationRepository.getCurrentConfiguration().getIteration()) {
            LOG.error("Specified Configuration Iteration must be either 1 or " +
                    classificationConfigurationRepository.getCurrentConfiguration().getIteration() + 1);
            System.exit(1);
        }
        xoManager.currentTransaction().begin();
        LOG.info("Preparing Data Set");
        xoManager.getRepository(TypeRepository.class).markAllInternalTypes("de.htw");
        xoManager.currentTransaction().commit();
        SARFRunner.activeClassificationConfiguration.materialize();
        TypeCouplingEnricher.enrich();
        LOG.info("Setting up Database Successful");
        return factory;
    }

    public static void readConfiguration() {
        LOG.info("Reading XML Configuration");
        try {
            URL schemaUrl = SARFRunner.class.getClassLoader().getResource("schema.xsd");
            URL configUrl = SARFRunner.class.getClassLoader().getResource("configuration_1.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(ActiveClassificationConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(schemaUrl);
            jaxbUnmarshaller.setSchema(schema);
            LOG.info("Unmarshalling XML Configuration");
            SARFRunner.activeClassificationConfiguration = (ActiveClassificationConfiguration) jaxbUnmarshaller.unmarshal(configUrl);
            activeClassificationConfiguration.addClassificationCriterion(new CohesionCriterion());
        } catch (JAXBException | SAXException e) {
            LOG.error(e.getMessage());
        }
        LOG.info("Unmarshalling XML Configuration Successful");
    }
}
