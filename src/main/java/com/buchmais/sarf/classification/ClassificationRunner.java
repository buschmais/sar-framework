package com.buchmais.sarf.classification;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.configuration.*;
import com.buchmais.sarf.classification.criterion.ClassificationInfoDescriptor;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.RuleDescriptor;
import com.buchmais.sarf.classification.criterion.cohesion.CohesionCriterion;
import com.buchmais.sarf.classification.criterion.cohesion.CohesionCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.dependency.*;
import com.buchmais.sarf.classification.criterion.packagenaming.PackageNamingCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.packagenaming.PackageNamingRepository;
import com.buchmais.sarf.classification.criterion.packagenaming.PackageNamingRuleDescriptor;
import com.buchmais.sarf.classification.criterion.typenaming.TypeNamingCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.typenaming.TypeNamingRepository;
import com.buchmais.sarf.classification.criterion.typenaming.TypeNamingRuleDescriptor;
import com.buchmais.sarf.metamodel.ComponentDependsOn;
import com.buchmais.sarf.metamodel.ComponentDescriptor;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buchmais.sarf.repository.MetricRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
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
import java.net.URL;
import java.util.Properties;

/**
 * @author Stephan Pirnbaum
 */
public class ClassificationRunner {

    private ActiveClassificationConfiguration activeClassificationConfiguration;

    private static final Logger LOG = LogManager.getLogger(ClassificationRunner.class);

    private static ClassificationRunner ourInstance = new ClassificationRunner();

    public static ClassificationRunner getInstance() {
        return ourInstance;
    }

    private ClassificationRunner() {
    }

    public void run(URI storeUri, URL configUrl, Integer iteration) {
        XOManagerFactory factory = setUpDB(storeUri);
        if (iteration != null) {
            loadIteration(iteration);
        } else {
            startNewIteration(configUrl);
        }
        factory.close();
    }

    public void startNewIteration() {

    }

    public void startNewIteration(URL configUrl) {

        if (configUrl != null) {
            this.activeClassificationConfiguration = readConfiguration(configUrl);
        }
        if (configUrl == null) {
            this.activeClassificationConfiguration = new ActiveClassificationConfiguration(1);
        }
        this.activeClassificationConfiguration.addClassificationCriterion(new CohesionCriterion());
        this.activeClassificationConfiguration.materialize();
        this.activeClassificationConfiguration.execute();
    }

    private ActiveClassificationConfiguration readConfiguration(URL configUrl) {
        ActiveClassificationConfiguration activeClassificationConfiguration = null;
        LOG.info("Reading XML Configuration");
        try {
            URL schemaUrl = SARFRunner.class.getClassLoader().getResource("schema.xsd");
            JAXBContext jaxbContext = JAXBContext.newInstance(ActiveClassificationConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(schemaUrl);
            jaxbUnmarshaller.setSchema(schema);
            LOG.info("Unmarshalling XML Configuration");
            activeClassificationConfiguration = (ActiveClassificationConfiguration) jaxbUnmarshaller.unmarshal(configUrl);
        } catch (JAXBException | SAXException e) {
            LOG.error(e);
        }
        LOG.info("Unmarshalling XML Configuration Successful");
        return activeClassificationConfiguration;
    }

    public ActiveClassificationConfiguration getCurrentIteration() {
        return null;
    }

    public ClassificationConfiguration loadIteration(Integer iteration) {
        return null;
    }

    private void setUpData() {
        ClassificationConfigurationRepository classificationConfigurationRepository = SARFRunner.xoManager.getRepository(ClassificationConfigurationRepository.class);
        if (this.activeClassificationConfiguration.getIteration() == 1) {
            LOG.info("Resetting Data");
            SARFRunner.xoManager.currentTransaction().begin();
            SARFRunner.xoManager.createQuery(
                    "MATCH (sarf:SARF) DETACH DELETE sarf"
            ).execute();
            SARFRunner.xoManager.createQuery(
                    "MATCH (:Type)-[c:COUPLES]-(:Type) DELETE c"
            ).execute();
            SARFRunner.xoManager.currentTransaction().commit();
            SARFRunner.xoManager.currentTransaction().begin();
            LOG.info("Preparing Data Set");
            SARFRunner.xoManager.getRepository(TypeRepository.class).markAllInternalTypes("de.htw");
            SARFRunner.xoManager.currentTransaction().commit();
            TypeCouplingEnricher.enrich();
        } else if (this.activeClassificationConfiguration.getIteration() <= classificationConfigurationRepository.getCurrentConfiguration().getIteration()) {
            LOG.error("Specified Configuration Iteration must be either 1 or " +
                    classificationConfigurationRepository.getCurrentConfiguration().getIteration() + 1);
            System.exit(1);
        }
    }

    private XOManagerFactory setUpDB(URI storeUri) {
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
        SARFRunner.xoManager = factory.createXOManager();
        LOG.info("Setting up Database Successful");
        return factory;
    }
}
