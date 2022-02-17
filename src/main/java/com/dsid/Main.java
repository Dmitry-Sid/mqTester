package com.dsid;

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
        PropertyConfigurator.configure(getClass().getClassLoader().getResource("log4j.properties"));
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error("Error", e));
        SpringFxmlLoader.init("Beans.xml");
        final Parent root = (Parent) SpringFxmlLoader.load("fxml/main.fxml");

        primaryStage.setTitle("mqTester");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
        Platform.setImplicitExit(true);
    }
}