package com.dsid.controllers;

import com.dsid.model.ControllerCommunicator;
import com.dsid.model.QueueManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigController extends AbstractController {
    private final QueueManager queueManager;

    @FXML
    private ComboBox<QueueManager.ClassProperties> connectionFactoryBox;

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
        super(controllerCommunicator);
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

        final List<QueueManager.ClassProperties> classPropertiesList = queueManager.getClasses();
        connectionFactoryBox.setButtonCell(new ComboBoxCell());
        connectionFactoryBox.setCellFactory(p -> new ComboBoxCell());
        connectionFactoryBox.setItems(FXCollections.observableArrayList(classPropertiesList));
        connectionFactoryBox.setOnAction(e -> {
            tableView.getItems().clear();
            addItems();
        });

        final QueueManager.Configuration configuration = queueManager.getConfiguration();
        if (configuration != null) {
            connectionFactoryBox.setValue(search(classPropertiesList, configuration.connectionFactoryClassName));
            if (!connectionFactoryBox.getItems().isEmpty()) {
                addItems();
            }
            queueInField.setText(configuration.queueIn);
            queueOutField.setText(configuration.queueOut);
            configuration.properties.forEach((property, value) -> search(property).setValue(value));
        } else {
            connectionFactoryBox.getSelectionModel().select(0);
        }
    }

    private void addItems() {
        connectionFactoryBox.getValue().properties.forEach(v -> tableView.getItems().add(new PropertyBean(v, "")));
    }

    private QueueManager.ClassProperties search(List<QueueManager.ClassProperties> classPropertiesList, String connectionFactoryClassName) {
        for (QueueManager.ClassProperties classProperties : classPropertiesList) {
            if (classProperties.clazz.getName().equals(connectionFactoryClassName)) {
                return classProperties;
            }
        }
        throw new IllegalArgumentException("unknown class " + connectionFactoryClassName);
    }

    private PropertyBean search(String property) {
        for (PropertyBean propertyBean : tableView.getItems()) {
            if (propertyBean.property.equals(property)) {
                return propertyBean;
            }
        }
        throw new IllegalArgumentException("unknown property " + property);
    }

    @FXML
    void save(ActionEvent event) {
        queueManager.configure(new QueueManager.Configuration(connectionFactoryBox.getValue().clazz.getName()
                , queueInField.getText()
                , queueOutField.getText()
                , tableView.getItems().stream()
                .filter(p -> StringUtils.isNotBlank(p.property) && StringUtils.isNotBlank(p.value))
                .collect(Collectors.toMap(PropertyBean::getProperty, PropertyBean::getValue))));
        controllerCommunicator.information("Mq connection successfully established");
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

    private static class ComboBoxCell extends ListCell<QueueManager.ClassProperties> {
        @Override
        protected void updateItem(QueueManager.ClassProperties classProperties, boolean flag) {
            super.updateItem(classProperties, flag);
            if (classProperties != null) {
                setText(classProperties.clazz.getName());
            }
        }
    }
}
