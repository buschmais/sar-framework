package com.buschmais.sarf.framework;

import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.SARFRunner;
import com.buschmais.sarf.benchmark.MoJoCalculator;
import com.buschmais.sarf.framework.configuration.*;
import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.sarf.plugin.cohesion.CohesionCriterion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
public class ClassificationRunner { // TODO: 18.07.2017 AbstractRunner + BenchmarkRunner

    private ActiveClassificationConfiguration activeClassificationConfiguration;

    private static final Logger LOG = LogManager.getLogger(ClassificationRunner.class);

    private static ClassificationRunner ourInstance = new ClassificationRunner();

    public static ClassificationRunner getInstance() {
        return ourInstance;
    }

    private ClassificationRunner() {
    }

    public Double run(URL configUrl, URL benchmarkUrl, Integer iteration) {
        //XOManagerFactory factory = setUpDB(storeUri);
        if (iteration != null) {
            loadIteration(iteration);
        } else if (benchmarkUrl != null) {
            return runBenchmark(benchmarkUrl);
        } else {
            startNewIteration(configUrl);
        }
        //factory.close();
        return 0d;
    }

    public Double runBenchmark(URL benchmarkUrl) {
        this.activeClassificationConfiguration = readConfiguration(benchmarkUrl);
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
    }

    public void startNewIteration() {

    }

    public void startNewIteration(Integer iteration, String artifact, String basePackage, String typeName,
                                  Integer generations, Integer populationSize, boolean hierarchical, boolean similarityBase) {
        this.activeClassificationConfiguration = new ActiveClassificationConfiguration(
                iteration, artifact, basePackage, typeName, generations, populationSize,
                hierarchical ? ClassificationConfiguration.Decomposition.DEEP : ClassificationConfiguration.Decomposition.FLAT,
                similarityBase ? ClassificationConfiguration.Optimization.SIMILARITY : ClassificationConfiguration.Optimization.COUPLING);
        this.activeClassificationConfiguration.addClassificationCriterion(new CohesionCriterion());
        this.setUpData();
        this.activeClassificationConfiguration.materialize();
        this.activeClassificationConfiguration.execute();

    }

    public void startNewIteration(URL configUrl) {

        if (configUrl != null) {
            this.activeClassificationConfiguration = readConfiguration(configUrl);
        }
        if (configUrl == null) {
            this.activeClassificationConfiguration = new ActiveClassificationConfiguration(1);
        }
        this.activeClassificationConfiguration.addClassificationCriterion(new CohesionCriterion());
        this.setUpData();
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
        DatabaseHelper.xoManager.currentTransaction().begin();
        ClassificationConfigurationRepository classificationConfigurationRepository = DatabaseHelper.xoManager.getRepository(ClassificationConfigurationRepository.class);
        if (this.activeClassificationConfiguration.getIteration() == 1) {
            LOG.info("Resetting Data");
            DatabaseHelper.xoManager.createQuery(
                    "MATCH (sarf:SARF) DETACH DELETE sarf"
            ).execute();
            DatabaseHelper.xoManager.createQuery(
                    "MATCH ()-[c:COUPLES]-() DELETE c"
            ).execute();
            DatabaseHelper.xoManager.createQuery(
                    "MATCH ()-[s:IS_SIMILAR_TO]-() DELETE s"
            ).execute();
            DatabaseHelper.xoManager.createQuery(
                    "MATCH (t:Type:Internal) REMOVE t:Internal"
            ).execute();
            DatabaseHelper.xoManager.currentTransaction().commit();
            DatabaseHelper.xoManager.currentTransaction().begin();
            LOG.info("Preparing Data Set");
            DatabaseHelper.xoManager.getRepository(TypeRepository.class).markAllInternalTypes(
                    this.activeClassificationConfiguration.getTypeName(),
                    this.activeClassificationConfiguration.getBasePackage(),
                    this.activeClassificationConfiguration.getArtifact());
            DatabaseHelper.xoManager.currentTransaction().commit();
            TypeCouplingEnricher.enrich();
        } else if (this.activeClassificationConfiguration.getIteration() <= classificationConfigurationRepository.getCurrentConfiguration().getIteration()) {
            LOG.error("Specified Configuration Iteration must be either 1 or " +
                    (classificationConfigurationRepository.getCurrentConfiguration().getIteration() + 1));
            DatabaseHelper.xoManager.currentTransaction().commit();
            System.exit(1);
        }
        if (DatabaseHelper.xoManager.currentTransaction().isActive())
            DatabaseHelper.xoManager.currentTransaction().commit();
    }
}