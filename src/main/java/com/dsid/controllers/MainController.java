package com.dsid.controllers;

import com.dsid.model.ControllerCommunicator;
import com.dsid.model.ObjectManager;
import com.dsid.model.QueueManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

public class MainController {
    private final ControllerCommunicator controllerCommunicator;
    private final QueueManager queueManager;
    private final ObjectManager objectManager;

    @FXML
    private MenuItem loadItem;

    @FXML
    private TextArea leftTextArea;

    @FXML
    private TextArea rightTextArea;

    @Inject
    public MainController(ControllerCommunicator controllerCommunicator, QueueManager queueManager, ObjectManager objectManager) {
        this.controllerCommunicator = controllerCommunicator;
        this.queueManager = queueManager;
        this.objectManager = objectManager;
        queueManager.onMessage(message -> Platform.runLater(() -> rightTextArea.setText(message)));
    }

    @FXML
    public void initialize() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jar Files", "*.jar*"));
        loadItem.setOnAction(event -> {
            final List<File> list = fileChooser.showOpenMultipleDialog(leftTextArea.getScene().getWindow());
            if (list != null) {
                list.forEach(file -> {
                    try {
                        objectManager.register(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }

    @FXML
    void configure(ActionEvent event) {
        controllerCommunicator.show("config", Collections.emptyMap());
    }

    @FXML
    void save(ActionEvent event) {

    }

    @FXML
    void send(ActionEvent event) {
        queueManager.send(leftTextArea.getText());
    }

}
