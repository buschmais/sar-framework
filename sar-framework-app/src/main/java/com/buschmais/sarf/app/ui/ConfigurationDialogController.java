package com.buschmais.sarf.app.ui;

import com.buschmais.sarf.core.framework.ClassificationRunner;
import com.buschmais.sarf.core.framework.configuration.ClassificationConfigurationXmlMapper;
import com.buschmais.sarf.core.framework.configuration.ConfigurationParser;
import com.buschmais.sarf.core.framework.configuration.Decomposition;
import com.buschmais.sarf.core.framework.configuration.Optimization;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URI;

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

    @FXML
    private TextField architecturePath;

    @FXML
    private Button chooseArchitecture;

    @Autowired
    @Lazy
    ClassificationRunner classificationRunner;

    @Autowired
    private ConfigurationParser configurationParser;

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
        this.chooseArchitecture.setOnAction(e -> this.selectArchitecture());
    }

    private void execute() {
        this.execute.setDisable(true);
        try {
            if (this.architecturePath.getText() != null && !this.architecturePath.getText().isEmpty()) {
                URI configUrl = new URI(this.architecturePath.getText());
                this.classificationRunner.startNewIteration(configUrl);
            } else {
                ClassificationConfigurationXmlMapper classificationConfiguration =
                    new ClassificationConfigurationXmlMapper();
                classificationConfiguration.iteration = 1;
                classificationConfiguration.artifact = this.artifact.getText();
                classificationConfiguration.basePackage = this.basePackage.getText();
                classificationConfiguration.typeName = this.typeName.getText();
                classificationConfiguration.generations = Integer.valueOf(this.generations.getText());
                classificationConfiguration.populationSize = Integer.valueOf(this.populationSize.getText());
                classificationConfiguration.decomposition = Decomposition.DEEP;
                classificationConfiguration.optimization = Optimization.COUPLING;
                this.classificationRunner.startNewIteration(classificationConfiguration);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionDialog("Execution Error", "An error occured during decomposing the system!", "", e);
        }
        this.execute.setDisable(false);
    }

    private void selectArchitecture() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Architecture Model");
        File f;
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("xml", "xml"));
        if ((f = chooser.showOpenDialog(this.chooseArchitecture.getScene().getWindow())) != null) {
            try {
                ClassificationConfigurationXmlMapper classificationConfigurationXmlMapper = this.configurationParser.readConfiguration(f.toURI());
                this.architecturePath.setText(f.toURI().toString());
                this.artifact.setText(classificationConfigurationXmlMapper.artifact);
                this.artifact.setDisable(true);
                this.basePackage.setText(classificationConfigurationXmlMapper.basePackage);
                this.basePackage.setDisable(true);
                this.typeName.setText(classificationConfigurationXmlMapper.typeName);
                this.typeName.setDisable(true);
                this.populationSize.setText(String.valueOf(classificationConfigurationXmlMapper.populationSize));
                this.populationSize.setDisable(true);
                this.generations.setText(String.valueOf(classificationConfigurationXmlMapper.generations));
                this.generations.setDisable(true);
            } catch (JAXBException | SAXException e) {
                e.printStackTrace();
                showExceptionDialog("Setup Error", "An error occured during opening the configuration!", "", e);
            }
        } else {
            this.artifact.setDisable(false);
            this.basePackage.setDisable(false);
            this.typeName.setDisable(false);
            this.populationSize.setDisable(false);
            this.generations.setDisable(false);
        }
    }

}
