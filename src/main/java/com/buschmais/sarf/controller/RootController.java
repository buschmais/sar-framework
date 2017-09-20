package com.buschmais.sarf.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * @author Stephan Pirnbaum
 */
public class RootController extends AbstractController {

    @FXML
    private BorderPane pane;

    @FXML
    public void initialize() {
        VBox box = new VBox();
        try {
            box.getChildren().add(new FXMLLoader(getClass().getResource("/views/database_connection.fxml")).load());
            box.getChildren().add(new FXMLLoader(getClass().getResource("/views/configuration_dialog.fxml")).load());
            pane.setCenter(box);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
