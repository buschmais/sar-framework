package com.buschmais.sarf.framework;

import com.buschmais.sarf.SARFRunner;
import com.buschmais.sarf.framework.configuration.*;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.sarf.plugin.cohesion.CohesionCriterionDescriptor;
import com.buschmais.xo.api.XOManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class ClassificationRunner { // TODO: 18.07.2017 AbstractRunner + BenchmarkRunner

    private final Logger LOG = LogManager.getLogger(ClassificationRunner.class);

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

    public Double run(URL configUrl, URL benchmarkUrl, Integer iteration) {
        if (benchmarkUrl != null) {
            return runBenchmark(benchmarkUrl);
        } else {
            startNewIteration(configUrl);
        }
        return 0d;
    }

    public Double runBenchmark(URL benchmarkUrl) {
        LOG.error("Benchmark functionality currently not available");
        return null;
        /*
        ClassificationConfigurationDescriptor classificationConfigurationDescriptor = readConfiguration(benchmarkUrl);
        this.setUpData();
        this.activeClassificationConfiguration.materialize();
        Set<ComponentDescriptor> reference = this.activeClassificationConfiguration.execute();
        MoJoCalculator.reference = reference;
        CohesionCriterion cohesionCriterion = new CohesionCriterion();
        Set<ComponentDescriptor> comp = cohesionCriterion.classify(2, null,
                this.activeClassificationConfiguration.getGenerations(), this.activeClassificationConfiguration.getPopulationSize(),
                false, this.activeClassificationConfiguration.getOptimization() == ClassificationConfiguration.Optimization.SIMILARITY);

        try {
            DatabaseHelper.xoManager.currentTransaction().begin();
            MoJoCalculator moJoCalculator1 = new MoJoCalculator(reference, comp);
            MoJoCalculator moJoCalculator2 = new MoJoCalculator(comp, reference);
            MoJoCalculator moJoFmCalculator = new MoJoCalculator(comp, reference);
            MoJoCalculator moJoPlusCalculator1 = new MoJoCalculator(reference, comp);
            MoJoCalculator moJoPlusCalculator2 = new MoJoCalculator(comp, reference);
            Long mojoCompRef = moJoCalculator1.mojo();
            Long mojoRefComp = moJoCalculator2.mojo();
            Long mojo = Math.min(mojoCompRef, mojoRefComp);
            Double mojoFm = moJoFmCalculator.mojofm();
            Long mojoPlusCompRef = moJoPlusCalculator1.mojoplus();
            Long mojoPlusRefComp = moJoPlusCalculator2.mojoplus();
            Long mojoPlus = Math.min(mojoPlusCompRef, mojoPlusRefComp);
            TypeRepository typeRepository = DatabaseHelper.xoManager.getRepository(TypeRepository.class);
            Long typeCount = typeRepository.countAllInternalTypes();
            try (FileWriter fW = new FileWriter("Result_Benchmark_" + System.currentTimeMillis())) {
                BufferedWriter bW = new BufferedWriter(fW);
                PrintWriter pW = new PrintWriter(bW);
                pW.println(WeightConstants.stringify());
                pW.println("MoJo Quality: " + (100 - (100. * mojo / typeCount)) + " %");
                pW.println("MoJoFM Quality: " + mojoFm + " %");
                pW.println("MoJo Plus Quality: " + (100 - (100. * mojoPlus / typeCount)) + " %");
                StringBuilder formatted = new StringBuilder();
                ActiveClassificationConfiguration.prettyPrint(comp, "", formatted);
                pW.println(formatted.toString());
                DatabaseHelper.xoManager.currentTransaction().commit();
                pW.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mojoFm;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return Double.MIN_VALUE;
        }
*/
    }

    public void startNewIteration(Integer iteration, String artifact, String basePackage, String typeName,
                                  Integer generations, Integer populationSize, boolean hierarchical, boolean similarityBase) {
        this.xOManager.currentTransaction().begin();
        if (iteration == 1) {
            LOG.info("Resetting Data");
            this.xOManager.createQuery(
                "MATCH (sarf:SARF) DETACH DELETE sarf"
            ).execute();
            this.xOManager.createQuery(
                "MATCH ()-[c:COUPLES]-() DELETE c"
            ).execute();
            this.xOManager.createQuery(
                "MATCH ()-[s:IS_SIMILAR_TO]-() DELETE s"
            ).execute();
            this.xOManager.createQuery(
                "MATCH (t:Type:Internal) REMOVE t:Internal"
            ).execute();
        }
        ClassificationConfigurationDescriptor descriptor = this.xOManager.create(ClassificationConfigurationDescriptor.class);
        descriptor.setIteration(iteration);
        descriptor.setArtifact(artifact);
        descriptor.setBasePackage(basePackage);
        descriptor.setTypeName(typeName);
        descriptor.setGenerations(generations);
        descriptor.setPopulationSize(populationSize);
        descriptor.setDecomposition(hierarchical ? "deep" : "flat");
        descriptor.setOptimization(similarityBase ? "similarity" : "coupling");
        descriptor.getClassificationCriteria().add(this.xOManager.create(CohesionCriterionDescriptor.class));
        this.xOManager.currentTransaction().commit();
        this.setUpData(descriptor);
        this.executor.execute(descriptor);
    }

    public void startNewIteration(URL configUrl) {
        ClassificationConfigurationDescriptor classificationConfigurationDescriptor = null;
        classificationConfigurationDescriptor = readConfiguration(configUrl);
        this.xOManager.currentTransaction().begin();
        classificationConfigurationDescriptor.getClassificationCriteria().add(this.xOManager.create(CohesionCriterionDescriptor.class));
        this.xOManager.currentTransaction().commit();
        this.setUpData(classificationConfigurationDescriptor);
        this.executor.execute(classificationConfigurationDescriptor);
    }

    private ClassificationConfigurationDescriptor readConfiguration(URL configUrl) {
        LOG.info("Reading XML Configuration");
        try {
            URL schemaUrl = SARFRunner.class.getClassLoader().getResource("schema.xsd");
            JAXBContext jaxbContext = JAXBContext.newInstance(ClassificationConfigurationXmlMapper.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(schemaUrl);
            jaxbUnmarshaller.setSchema(schema);
            LOG.info("Unmarshalling XML Configuration");
            ClassificationConfigurationXmlMapper mapper= (ClassificationConfigurationXmlMapper) jaxbUnmarshaller.unmarshal(configUrl);
            ClassificationConfigurationDescriptor descriptor = this.materializer.materialize(mapper);
            LOG.info("Unmarshalling XML Configuration Successful");
            return descriptor;
        } catch (JAXBException | SAXException e) {
            LOG.error(e);
            System.exit(1);
            return null;
        }
    }

    private void setUpData(ClassificationConfigurationDescriptor descriptor) {
        this.xOManager.currentTransaction().begin();
        ClassificationConfigurationRepository classificationConfigurationRepository = this.xOManager.getRepository(ClassificationConfigurationRepository.class);
        if (descriptor.getIteration() == 1) {
            LOG.info("Preparing Data Set");
            this.xOManager.getRepository(TypeRepository.class).markAllInternalTypes(
                    descriptor.getTypeName(),
                    descriptor.getBasePackage(),
                    descriptor.getArtifact());
            this.xOManager.currentTransaction().commit();
            this.typeCouplingEnricher.enrich();
        } else if (descriptor.getIteration() <= classificationConfigurationRepository.getCurrentConfiguration().getIteration()) {
            LOG.error("Specified Configuration Iteration must be either 1 or " +
                    (classificationConfigurationRepository.getCurrentConfiguration().getIteration() + 1));
            this.xOManager.currentTransaction().commit();
            System.exit(1);
        }
        if (this.xOManager.currentTransaction().isActive())
            this.xOManager.currentTransaction().commit();
    }
}
