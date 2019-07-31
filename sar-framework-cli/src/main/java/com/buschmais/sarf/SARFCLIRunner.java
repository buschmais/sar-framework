package com.buschmais.sarf;

import com.buschmais.sarf.core.framework.ClassificationRunner;
import com.buschmais.xo.api.XOManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by steph on 04.05.2017.
 */
@SpringBootApplication
@Slf4j
public class SARFCLIRunner {

    public static void main(String[] args) throws URISyntaxException {
        ConfigurableApplicationContext springContext = SpringApplication.run(SARFCLIRunner.class);

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

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            URI storeUri = new URI(cmd.getOptionValue("s"));
            URI configUri = cmd.hasOption("c") ? new URI(cmd.getOptionValue("c")) : null;
            Integer iteration = cmd.hasOption("l") ? Integer.valueOf(cmd.getOptionValue("l")) : null;
            URI benchUri = cmd.hasOption("b") ? new URI(cmd.getOptionValue("b")) : null;

            springContext.getBean(XOManager.class, storeUri);
            ClassificationRunner runner = springContext.getBean(ClassificationRunner.class);
            runner.startNewIteration(configUri);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            formatter.printHelp("java -jar sarf.jar", options);
            System.exit(1);
        } catch (JAXBException | SAXException e) {
            LOGGER.error("Configuration file could not be parsed", e);
            System.exit(1);
        }
        System.exit(0);
    }
}
