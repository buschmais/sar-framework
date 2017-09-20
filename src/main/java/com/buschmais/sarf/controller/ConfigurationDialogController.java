package com.buschmais.sarf.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 * @author Stephan Pirnbaum
 */
public class ConfigurationDialogController extends AbstractController {

    @FXML
    private ChoiceBox<Integer> iteration;

    @FXML
    private TextField basePackage;

    @FXML
    private CheckBox basePackageAsRegEx;

    @FXML
    private TextField artifact;

    @FXML
    private TextField typeName;

    @FXML
    private CheckBox artifactAsRegEx;

    @FXML
    private CheckBox typeNameAsRegEx;

    @FXML
    private ChoiceBox<String> decomposition;

    @FXML
    private ChoiceBox<String> strategy;

    @FXML
    public void initialize() {
        this.iteration.getItems().add(1);
        this.decomposition.getItems().add("Flat");
        this.decomposition.getItems().add("Hierarchical");
        this.decomposition.setValue("Hierarchical");
        this.strategy.getItems().add("Coupling");
        this.strategy.getItems().add("Similarity");
        this.strategy.setValue("Coupling");
    }
}
