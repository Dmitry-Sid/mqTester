package com.dsid.model.impl;

import com.dsid.model.ObjectConverter;
import com.dsid.model.ObjectManager;
import com.dsid.model.QueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jms.*;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QueueManagerImpl implements QueueManager {
    private static final Logger log = LoggerFactory.getLogger(QueueManagerImpl.class);

    private final ObjectManager objectManager;
    private final ObjectConverter objectConverter;
    private final String fileName;

    private Configuration configuration;
    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;
    private MessageConsumer messageConsumer;
    private Consumer<String> consumer;

    @Inject
    public QueueManagerImpl(ObjectManager objectManager, ObjectConverter objectConverter, String fileName) {
        this.objectManager = objectManager;
        this.objectConverter = objectConverter;
        this.fileName = fileName;
        if (new File(fileName).exists()) {
            this.configuration = objectConverter.fromFile(Configuration.class, fileName);
            if (configuration != null) {
                configure(configuration);
            }
        }
        // Закешировать
        objectManager.getSuccessors(ConnectionFactory.class);
    }

    @Override
    public void configure(Configuration configuration) {
        try {
            destroy();
            final ConnectionFactory connectionFactory = objectManager.create(ConnectionFactory.class, configuration.connectionFactoryClassName, configuration.properties);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            messageProducer = createProducer(configuration.queueIn);
            messageConsumer = createConsumer(configuration.queueOut);
            this.configuration = configuration;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public List<ClassProperties> getClasses() {
        return objectManager.getSuccessors(ConnectionFactory.class)
                .stream().map(c -> new ClassProperties(c, objectManager.getProperties(c))).collect(Collectors.toList());
    }

    private MessageProducer createProducer(String queueName) throws JMSException {
        final MessageProducer producer = session.createProducer(session.createQueue(queueName));
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return producer;
    }

    private MessageConsumer createConsumer(String queueName) throws JMSException {
        final MessageConsumer messageConsumer = session.createConsumer(session.createQueue(queueName));
        messageConsumer.setMessageListener(message -> {
            if (message instanceof TextMessage) {
                try {
                    consumer.accept(((TextMessage) message).getText());
                } catch (Exception e) {
                    log.error("error", e);
                }
            }
        });
        return messageConsumer;
    }

    @Override
    public void send(String message) {
        try {
            messageProducer.send(session.createTextMessage(message));
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void save() {
        objectConverter.toFile(configuration, fileName);
    }

    public void destroy() throws JMSException {
        if (messageConsumer != null) {
            messageConsumer.close();
        }
        if (messageProducer != null) {
            messageProducer.close();
        }
        if (session != null) {
            session.close();
        }
        if (connection != null) {
            connection.close();
        }
    }
}
