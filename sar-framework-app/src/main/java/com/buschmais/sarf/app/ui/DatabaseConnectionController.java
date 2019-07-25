package com.buschmais.sarf.app.ui;

import com.buschmais.xo.api.XOManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Stephan Pirnbaum
 */
@Controller
@RequiredArgsConstructor
public class DatabaseConnectionController extends AbstractController {

    @FXML
    private Button connect;

    @FXML
    private Button disconnect;

    @FXML
    private Button choose;

    @FXML
    private TextField storePath;

    private final BeanFactory beanFactory;

    @Autowired
    @Lazy
    private XOManager xoManager;

    @FXML
    public void initialize() {
        this.connect.setOnAction(e -> this.connectToDatabase());
        this.disconnect.setOnAction(e -> this.disconnectFromDatabase());
        this.choose.setOnAction(e -> this.selectStore());

    }

    private void connectToDatabase() {
        boolean successful = false;
        try {
            beanFactory.getBean(XOManager.class, new URI(this.storePath.getText()));
            successful = true;
        } catch (URISyntaxException e) {
            showExceptionDialog("Database Setup Error", "An error occurred during database setup", "Invalid URI entered", e);
        } catch (RuntimeException e) {
            showExceptionDialog("Database Setup Error", "An error occurred during database setup", "Not a valid store directory!", e);
        }
        if (successful) {
            this.connect.setDisable(true);
            this.disconnect.setDisable(false);
        }
    }

    private void disconnectFromDatabase() {
        boolean successful = false;
        try {
            xoManager.close();
            successful = true;
        } catch (Exception e) {
            showExceptionDialog("Database Shutdown error", "An error occurred during database shutdown", "", e);
        }
        if (successful) {
            this.connect.setDisable(false);
            this.disconnect.setDisable(true);
        }
    }

    private void selectStore() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Store Path");
        File f;
        if ((f = chooser.showDialog(this.choose.getScene().getWindow())) != null) {
            this.storePath.setText(f.toURI().toString());
            this.connect.setDisable(false);
        }
    }
}
