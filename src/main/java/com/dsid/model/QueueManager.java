package com.dsid.model;

import java.util.Map;
import java.util.function.Consumer;

public interface QueueManager {
    void configure(Configuration configuration);

    Configuration getConfiguration();

    void send(String message);

    void onMessage(Consumer<String> consumer);

    class Configuration {
        public final String className;
        public final String queueIn;
        public final String queueOut;
        public final Map<String, String> properties;

        public Configuration(String className, String queueIn, String queueOut, Map<String, String> properties) {
            this.className = className;
            this.queueIn = queueIn;
            this.queueOut = queueOut;
            this.properties = properties;
        }
    }
}
