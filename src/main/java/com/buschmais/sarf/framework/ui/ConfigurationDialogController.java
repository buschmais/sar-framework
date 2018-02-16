package com.buschmais.sarf.framework.ui;

import com.buschmais.sarf.framework.ClassificationRunner;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

/**
 * @author Stephan Pirnbaum
 */
@Controller
public class ConfigurationDialogController extends AbstractController {

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
    private Button execute;

    @FXML
    private ProgressBar progress;

    @FXML
    private TextField generations;

    @FXML
    private TextField populationSize;

    @Autowired
    @Lazy
    ClassificationRunner classificationRunner;

    @FXML
    public void initialize() {
        this.execute.setOnAction(e -> this.execute());
        this.artifactAsRegEx.setDisable(true);
        this.basePackageAsRegEx.setDisable(true);
        this.typeNameAsRegEx.setDisable(true);
        this.generations.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                generations.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        this.populationSize.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                generations.setText(newValue.replaceAll("[^\\d]", ""));
            }
        }));
    }


    private void execute() {
        this.execute.setDisable(true);
        try {
            this.classificationRunner.startNewIteration(
                    1, this.artifact.getText(), this.basePackage.getText(), this.typeName.getText(),
                    Integer.valueOf(this.generations.getText()), Integer.valueOf(this.populationSize.getText()), true, false
            );
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionDialog("Execution Error", "An error occured during decomposing the system!", "", e);
        }
        this.execute.setDisable(false);
    }


}
