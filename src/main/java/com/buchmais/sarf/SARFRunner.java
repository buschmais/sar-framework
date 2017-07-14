package com.buchmais.sarf;

import com.buchmais.sarf.classification.ClassificationRunner;
import com.buschmais.xo.api.XOManager;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by steph on 04.05.2017.
 */
public class SARFRunner {

    private static final Logger LOG = LogManager.getLogger(SARFRunner.class);

    public static XOManager xoManager;

    public static void main(String[] args) throws URISyntaxException {
        ClassificationRunner runner = ClassificationRunner.getInstance();
        Options options = new Options();
        Option store = new Option("s", "store", true, "Neo4J Graph Store Path");
        store.setRequired(true);
        Option config = new Option("c", "conf", true, "Configuration File Path");
        config.setRequired(false);
        Option load = new Option("l", "load", true, "Iteration to load");
        load.setRequired(false);
        OptionGroup group = new OptionGroup();
        group.addOption(config);
        group.addOption(load);
        group.setRequired(false);
        options.addOption(store);
        options.addOptionGroup(group);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            URI storeUri = new URI(cmd.getOptionValue("s"));
            URL configUrl = cmd.hasOption("c") ? new URL(cmd.getOptionValue("c")) : null;
            Integer iteration = cmd.hasOption("l") ? Integer.valueOf(cmd.getOptionValue("l")) : null;
            runner.run(storeUri, configUrl, iteration);
        } catch (ParseException e) {
            LOG.error(e.getMessage());
            formatter.printHelp("java -jar sarf.jar",options);
            System.exit(1);
        } catch (MalformedURLException e) {
            LOG.error("Configuration file not found");
            System.exit(1);
        }
    }


}
