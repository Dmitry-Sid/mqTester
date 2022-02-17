package com.dsid.controllers;

import com.dsid.model.ControllerCommunicator;
import com.dsid.model.QueueManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class ConfigController {
    private final ControllerCommunicator controllerCommunicator;
    private final QueueManager queueManager;

    @FXML
    private TextField connectionFactoryField;

    @FXML
    private TextField queueInField;

    @FXML
    private TextField queueOutField;

    @FXML
    private TableView<PropertyBean> tableView;

    @FXML
    private TableColumn<PropertyBean, String> propertyColumn;

    @FXML
    private TableColumn<PropertyBean, String> valueColumn;

    @Inject
    public ConfigController(ControllerCommunicator controllerCommunicator, QueueManager queueManager) {
        this.controllerCommunicator = controllerCommunicator;
        this.queueManager = queueManager;
    }

    @FXML
    public void initialize() {
        propertyColumn.setCellValueFactory(new PropertyValueFactory<>("property"));
        propertyColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        propertyColumn.setOnEditCommit(event -> {
            final PropertyBean propertyBean = event.getRowValue();
            propertyBean.setProperty(event.getNewValue());
        });

        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setOnEditCommit(event -> {
            final PropertyBean propertyBean = event.getRowValue();
            propertyBean.setValue(event.getNewValue());
        });

        final QueueManager.Configuration configuration = queueManager.getConfiguration();
        if (configuration != null) {
            connectionFactoryField.setText(configuration.className);
            queueInField.setText(configuration.queueIn);
            queueOutField.setText(configuration.queueOut);
            configuration.properties.forEach((property, value) -> tableView.getItems().add(new PropertyBean(property, value)));
        }
    }


    @FXML
    void addProperty(ActionEvent event) {
        tableView.getItems().add(new PropertyBean("", ""));
    }

    @FXML
    void save(ActionEvent event) {
        queueManager.configure(new QueueManager.Configuration(connectionFactoryField.getText()
                , queueInField.getText()
                , queueOutField.getText()
                , tableView.getItems().stream()
                .filter(p -> StringUtils.isNotBlank(p.property) && StringUtils.isNotBlank(p.value))
                .collect(Collectors.toMap(PropertyBean::getProperty, PropertyBean::getValue))));
    }

    public static class PropertyBean {
        private String property;
        private String value;

        private PropertyBean(String property, String value) {
            this.property = property;
            this.value = value;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
