package com.buschmais.sarf.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by steph on 04.05.2017.
 */
@SpringBootApplication
public class SARFRunner extends Application {

    private ConfigurableApplicationContext springContext;

    private Parent rootNode;

    public static void main(String[] args) {
        launch(SARFRunner.class, args);
    }

    @Override
    public void init() throws Exception {
        this.springContext = SpringApplication.run(SARFRunner.class);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/root.fxml"));
        fxmlLoader.setControllerFactory(this.springContext::getBean);
        this.rootNode = fxmlLoader.load();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("SAR-Framework");
        primaryStage.setScene(new Scene(this.rootNode));
        primaryStage.show();
    }
}
