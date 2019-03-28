package com.buschmais.sarf.framework;

import com.buschmais.sarf.SARFRunner;
import com.buschmais.sarf.framework.configuration.*;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.sarf.plugin.cohesion.CohesionCriterionDescriptor;
import com.buschmais.xo.api.XOManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.net.URL;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
@Slf4j
public class ClassificationRunner implements AbstractRunner {

    private XOManager xOManager;

    private ClassificationConfigurationMaterializer materializer;

    private ClassificationConfigurationExecutor executor;

    private TypeCouplingEnricher typeCouplingEnricher;

    @Autowired
    public ClassificationRunner(XOManager xOManager, ClassificationConfigurationMaterializer materializer,
        ClassificationConfigurationExecutor executor, TypeCouplingEnricher typeCouplingEnricher) {
        this.xOManager = xOManager;
        this.materializer = materializer;
        this.executor = executor;
        this.typeCouplingEnricher = typeCouplingEnricher;
    }

    @Override
    public Double run(URL configUrl) {
        startNewIteration(configUrl);
        return 0d;
    }

    public void startNewIteration(ClassificationConfigurationXmlMapper configuration) {
        this.xOManager.currentTransaction().begin();
        if (configuration.iteration == 1) {
            LOGGER.info("Resetting Data");
            this.xOManager.createQuery("MATCH (sarf:SARF) DETACH DELETE sarf").execute();
            this.xOManager.createQuery("MATCH ()-[c:COUPLES]-() DELETE c").execute();
            this.xOManager.createQuery("MATCH ()-[s:IS_SIMILAR_TO]-() DELETE s").execute();
            this.xOManager.createQuery("MATCH (t:Type:Internal) REMOVE t:Internal").execute();
        }
        ClassificationConfigurationDescriptor descriptor = materializer.materialize(configuration);
        descriptor.getClassificationCriteria().add(this.xOManager.create(CohesionCriterionDescriptor.class));
        this.xOManager.currentTransaction().commit();
        this.setUpData(descriptor);
        this.executor.execute(descriptor);
    }

    public void startNewIteration(URL configUrl) {
        startNewIteration(readConfiguration(configUrl));
    }

    private ClassificationConfigurationXmlMapper readConfiguration(URL configUrl) {
        LOGGER.info("Reading XML Configuration");
        try {
            URL schemaUrl = SARFRunner.class.getClassLoader().getResource("schema.xsd");
            JAXBContext jaxbContext = JAXBContext.newInstance(ClassificationConfigurationXmlMapper.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(schemaUrl);
            jaxbUnmarshaller.setSchema(schema);
            LOGGER.info("Unmarshalling XML Configuration");
            ClassificationConfigurationXmlMapper mapper =
                (ClassificationConfigurationXmlMapper) jaxbUnmarshaller.unmarshal(configUrl);
            LOGGER.info("Unmarshalling XML Configuration Successful");
            return mapper;
        } catch (JAXBException | SAXException e) {
            LOGGER.error("Unable to read configuration", e);
            System.exit(1);
            return null;
        }
    }

    private void setUpData(ClassificationConfigurationDescriptor descriptor) {
        this.xOManager.currentTransaction().begin();
        ClassificationConfigurationRepository classificationConfigurationRepository =
            this.xOManager.getRepository(ClassificationConfigurationRepository.class);
        if (descriptor.getIteration() == 1) {
            LOGGER.info("Preparing Data Set");
            this.xOManager.getRepository(TypeRepository.class)
                .markAllInternalTypes(descriptor.getTypeName(), descriptor.getBasePackage(),
                    descriptor.getArtifact());
            this.xOManager.currentTransaction().commit();
            this.typeCouplingEnricher.enrich();
        } else if (descriptor.getIteration() <= classificationConfigurationRepository
            .getCurrentConfiguration().getIteration()) {
            LOGGER.error("Specified Configuration Iteration must be either 1 or {}",
                classificationConfigurationRepository.getCurrentConfiguration().getIteration() + 1);
            this.xOManager.currentTransaction().commit();
            System.exit(1);
        }
        if (this.xOManager.currentTransaction().isActive()) {
            this.xOManager.currentTransaction().commit();
        }
    }
}
