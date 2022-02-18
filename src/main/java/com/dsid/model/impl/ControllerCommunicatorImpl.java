package com.dsid.model.impl;

import com.dsid.model.ControllerCommunicator;
import com.dsid.model.SpringFxmlLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class ControllerCommunicatorImpl implements ControllerCommunicator {
    private Map<String, Object> parameters;

    @Override
    public void show(String name, Map<String, Object> parameters) {
        this.parameters = parameters;
        final Parent root = (Parent) SpringFxmlLoader.load("fxml/" + name + ".fxml");
        final Stage stage = new Stage();
        stage.setTitle("MqTester");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public void information(String message) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void error(Throwable e) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("An exception occurred!");
        alert.getDialogPane().setExpandableContent(new ScrollPane(new TextArea(getStackTrace(e))));
        alert.showAndWait();
    }

    private String getStackTrace(Throwable e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
