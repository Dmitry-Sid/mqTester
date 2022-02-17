package com.dsid.model.impl;

import com.dsid.model.ControllerCommunicator;
import com.dsid.model.SpringFxmlLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Map;

public class ControllerCommunicatorImpl implements ControllerCommunicator {
    private Map<String, Object> parameters;

    @Override
    public void show(String name, Map<String, Object> parameters) {
        final Parent root = (Parent) SpringFxmlLoader.load("fxml/" + name + ".fxml");
        final Stage stage = new Stage();
        stage.setTitle("mqTester");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
