package com.buschmais.sarf;

import com.buschmais.sarf.classification.ClassificationRunner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by steph on 04.05.2017.
 */
public class SARFRunner extends Application{

    private static final Logger LOG = LogManager.getLogger(SARFRunner.class);

    private ConfigurableApplicationContext springContext;

    private Stage primaryStage;

    private BorderPane rootLayout;

    public static void main(String[] args) throws URISyntaxException {
        if (args.length == 0) {
            launch(SARFRunner.class, args);
        } else {
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
                DatabaseHelper.setUpDB(storeUri);
                runner.run(configUrl, benchUrl, iteration);
                DatabaseHelper.stopDB();
            } catch (ParseException e) {
                LOG.error(e.getMessage());
                formatter.printHelp("java -jar sarf.jar", options);
                System.exit(1);
            } catch (MalformedURLException e) {
                LOG.error("Configuration file not found");
                System.exit(1);
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("SAR-Framework");
        springContext = SpringApplication.run(SARFRunner.class);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/root.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        this.rootLayout = fxmlLoader.load();
        Scene scene = new Scene(this.rootLayout);
        this.primaryStage.setScene(scene);
        this.primaryStage.show();
        fxmlLoader = new FXMLLoader(getClass().getResource("/views/database_connection.fxml"));
        this.rootLayout.setCenter(fxmlLoader.load());
    }
}
