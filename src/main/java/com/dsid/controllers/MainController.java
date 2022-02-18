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
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainController extends AbstractController {
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
        super(controllerCommunicator);
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
            if (list != null && !list.isEmpty()) {
                list.forEach(file -> {
                    try {
                        final File copiedFile = new File(file.getName());
                        if (!file.getAbsolutePath().equals(copiedFile.getAbsolutePath())) {
                            FileUtils.copyFile(file, copiedFile);
                        }
                        objectManager.register(copiedFile.toURI().toURL());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                controllerCommunicator.information("Files uploaded successfully");
            }
        });
    }

    @FXML
    void configure(ActionEvent event) {
        controllerCommunicator.show("config", Collections.emptyMap());
    }

    @FXML
    void save(ActionEvent event) {
        queueManager.save();
        objectManager.save();
        controllerCommunicator.information("Project saved successfully");
    }

    @FXML
    void send(ActionEvent event) {
        queueManager.send(leftTextArea.getText());
    }
}
