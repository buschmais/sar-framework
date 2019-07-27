package com.buschmais.sarf.core.framework.configuration;

import com.buschmais.sarf.core.framework.ClassificationRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.URI;
import java.net.URL;

@Slf4j
@Service
public class ConfigurationParser {

    public ClassificationConfigurationXmlMapper readConfiguration(URI configUri) throws JAXBException, SAXException {
        LOGGER.info("Reading XML Configuration");

        URL schemaUrl = ClassificationRunner.class.getClassLoader().getResource("schema.xsd");
        JAXBContext jaxbContext = JAXBContext.newInstance(ClassificationConfigurationXmlMapper.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(schemaUrl);
        jaxbUnmarshaller.setSchema(schema);
        LOGGER.info("Unmarshalling XML Configuration");
        ClassificationConfigurationXmlMapper mapper =
            (ClassificationConfigurationXmlMapper) jaxbUnmarshaller.unmarshal(new File(configUri));
        LOGGER.info("Unmarshalling XML Configuration Successful");
        return mapper;
    }

}
