package com.dsid.model;

import javax.jms.ConnectionFactory;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface QueueManager extends Savable {
    void configure(Configuration configuration);

    Configuration getConfiguration();

    List<ClassProperties> getClasses();

    void send(String message);

    void onMessage(Consumer<String> consumer);

    class Configuration implements Serializable {
        public final String connectionFactoryClassName;
        public final String queueIn;
        public final String queueOut;
        public final Map<String, String> properties;

        public Configuration(String connectionFactoryClassName, String queueIn, String queueOut, Map<String, String> properties) {
            this.connectionFactoryClassName = connectionFactoryClassName;
            this.queueIn = queueIn;
            this.queueOut = queueOut;
            this.properties = properties;
        }
    }

    class ClassProperties {
        public final Class<? extends ConnectionFactory> clazz;
        public final Set<String> properties;

        public ClassProperties(Class<? extends ConnectionFactory> clazz, Set<String> properties) {
            this.clazz = clazz;
            this.properties = properties;
        }
    }
}
