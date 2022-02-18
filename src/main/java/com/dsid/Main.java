package com.dsid;

import com.dsid.model.ControllerCommunicator;
import com.dsid.model.SpringFxmlLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        SpringFxmlLoader.init("Beans.xml");
        PropertyConfigurator.configure(getClass().getClassLoader().getResource("log4j.properties"));
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            log.error("Error", e);
            SpringFxmlLoader.getContext().getBean(ControllerCommunicator.class).error(e);
        });
        final Parent root = (Parent) SpringFxmlLoader.load("fxml/main.fxml");
        primaryStage.setTitle("MqTester");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        Platform.setImplicitExit(true);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }
}