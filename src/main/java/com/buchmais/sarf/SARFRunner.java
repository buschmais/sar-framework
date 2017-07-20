package com.buchmais.sarf;

import com.buchmais.sarf.classification.ClassificationRunner;
import com.buchmais.sarf.classification.configuration.ClassificationConfigurationDescriptor;
import com.buchmais.sarf.classification.configuration.ClassificationConfigurationRepository;
import com.buchmais.sarf.classification.criterion.ClassificationInfoDescriptor;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterionDescriptor;
import com.buchmais.sarf.classification.criterion.RuleDescriptor;
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
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
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

    public static void main(String[] args) throws URISyntaxException {

        Options options = new Options();
        Option store = new Option("s", "store", true, "Neo4J Graph Store Path");
        store.setRequired(true);
        Option config = new Option("c", "conf", true, "Configuration File Path");
        config.setRequired(false);
        Option load = new Option("l", "load", true, "Iteration to load");
        load.setRequired(false);
        Option bench = new Option("b", "bench", true, "Benchmark File for Genetic Algorithm");
        bench.setRequired(false);
        OptionGroup group = new OptionGroup();
        group.addOption(config);
        group.addOption(load);
        group.addOption(bench);
        group.setRequired(false);
        options.addOption(store);
        options.addOptionGroup(group);
        ClassificationRunner runner = ClassificationRunner.getInstance();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            URI storeUri = new URI(cmd.getOptionValue("s"));
            URL configUrl = cmd.hasOption("c") ? new URL(cmd.getOptionValue("c")) : null;
            Integer iteration = cmd.hasOption("l") ? Integer.valueOf(cmd.getOptionValue("l")) : null;
            URL benchUrl = cmd.hasOption("b") ? new URL(cmd.getOptionValue("b")) : null;
            setUpDB(storeUri);
            runner.run(storeUri, configUrl, benchUrl, iteration);
        } catch (ParseException e) {
            LOG.error(e.getMessage());
            formatter.printHelp("java -jar sarf.jar", options);
            System.exit(1);
        } catch (MalformedURLException e) {
            LOG.error("Configuration file not found");
            System.exit(1);
        }
    }

    public static XOManagerFactory setUpDB(URI storeUri) {
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
