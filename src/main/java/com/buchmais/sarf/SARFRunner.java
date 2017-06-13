package com.buchmais.sarf;

import com.buchmais.sarf.classification.configuration.ActiveClassificationConfiguration;
import com.buchmais.sarf.classification.configuration.ConfigurationHistory;
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
                .uri(new URI("file:///E:/Development/trainingszeitverwaltung-kraftraum/target/jqassistant/store"))
                .build();
        XOManagerFactory factory = XO.createXOManagerFactory(xoUnit);
        xoManager = factory.createXOManager();
        xoManager.currentTransaction().begin();
        xoManager.getRepository(TypeRepository.class).markAllInternalTypes("de.htw");
        xoManager.currentTransaction().commit();
        ClassificationConfigurationRepository classificationConfigurationRepository = SARFRunner.xoManager.getRepository(ClassificationConfigurationRepository.class);
        if (SARFRunner.activeClassificationConfiguration.getIteration() == 1) {
            SARFRunner.xoManager.currentTransaction().begin();
            SARFRunner.xoManager.createQuery(
                    "MATCH (sarf:SARF) DETACH DELETE sarf"
            ).execute();
            SARFRunner.xoManager.currentTransaction().commit();
        } else if (SARFRunner.activeClassificationConfiguration.getIteration() <= classificationConfigurationRepository.getCurrentConfiguration().getIteration()) {
            System.err.println("Specified Configuration Iteration must be either 1 or " +
                    classificationConfigurationRepository.getCurrentConfiguration().getIteration());
            System.exit(1);
        }
        SARFRunner.activeClassificationConfiguration.materialize();
        return factory;
    }

    public static void readConfiguration() {
        try {
            URL schemaUrl = SARFRunner.class.getClassLoader().getResource("schema.xsd");
            URL configUrl = SARFRunner.class.getClassLoader().getResource("configuration.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(ActiveClassificationConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(schemaUrl);
            jaxbUnmarshaller.setSchema(schema);
            SARFRunner.activeClassificationConfiguration = (ActiveClassificationConfiguration) jaxbUnmarshaller.unmarshal(configUrl);
        } catch (JAXBException | SAXException e) {
            e.printStackTrace();
        }
    }
}
