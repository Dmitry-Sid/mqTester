package com.dsid.model;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;

public class SpringFxmlLoader {

    private static ApplicationContext context;

    public static void init(String springXMLFile) {
        context = new ClassPathXmlApplicationContext(springXMLFile);
    }

    public static Object load(String url) {
        if (context == null) {
            throw new IllegalStateException("context is not initialized");
        }
        try (InputStream fxmlStream = SpringFxmlLoader.class.getClassLoader().getResourceAsStream(url)) {
            final FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(context::getBean);
            return loader.load(fxmlStream);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
