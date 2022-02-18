package test;

import com.dsid.model.ObjectConverter;
import com.dsid.model.QueueManager;
import com.dsid.model.impl.ObjectConverterImpl;
import com.dsid.model.impl.ObjectManagerImpl;
import com.dsid.model.impl.QueueManagerImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Queue;
import javax.jms.*;
import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class QueueManagerTest {
    private final ObjectConverter objectConverter = new ObjectConverterImpl();
    private final QueueManager queueManager = new QueueManagerImpl(new ObjectManagerImpl(objectConverter, "test"), objectConverter, "testQueue");

    @Before
    public void before() {
        new File("test").delete();
    }

    @After
    public void after() {
        new File("test").delete();
    }

    @Test
    public void fullTest() {
        {
            final MessageMaker messageMaker = new MessageMaker("testVal1", "testVal2", "queueIn", "queueOut");

            final boolean[] calls = new boolean[]{false, false};
            final Map<String, String> map = new HashMap<String, String>() {{
                put("value1", messageMaker.value1);
                put("value2", messageMaker.value2);
            }};
            queueManager.configure(new QueueManager.Configuration(ConnectionFactoryTest.class.getName(), messageMaker.queueIn, messageMaker.queueOut, map));
            {
                final QueueManager.Configuration configuration = queueManager.getConfiguration();
                assertEquals(ConnectionFactoryTest.class.getName(), configuration.connectionFactoryClassName);
                assertEquals(messageMaker.queueIn, configuration.queueIn);
                assertEquals(messageMaker.queueOut, configuration.queueOut);
                assertEquals(map, configuration.properties);
            }

            queueManager.onMessage(message -> {
                assertEquals(messageMaker.makeMessage("test"), message);
                calls[0] = true;
            });
            queueManager.send("test");
            assertTrue(calls[0]);

            queueManager.onMessage(message -> {
                assertEquals(messageMaker.makeMessage("test2"), message);
                calls[1] = true;
            });
            queueManager.send("test2");
            assertTrue(calls[1]);
        }
        {
            final MessageMaker messageMaker = new MessageMaker("testVal3", null, "queueIn", "queueOut");

            final boolean[] calls = new boolean[]{false, false};
            final Map<String, String> map = new HashMap<String, String>() {{
                put("value1", messageMaker.value1);
                put("value2", messageMaker.value2);
            }};
            queueManager.configure(new QueueManager.Configuration(ConnectionFactoryTest.class.getName(), messageMaker.queueIn, messageMaker.queueOut, map));
            {
                final QueueManager.Configuration configuration = queueManager.getConfiguration();
                assertEquals(ConnectionFactoryTest.class.getName(), configuration.connectionFactoryClassName);
                assertEquals(messageMaker.queueIn, configuration.queueIn);
                assertEquals(messageMaker.queueOut, configuration.queueOut);
                assertEquals(map, configuration.properties);
            }

            queueManager.onMessage(message -> {
                assertEquals(messageMaker.makeMessage("test3"), message);
                calls[0] = true;
            });
            queueManager.send("test3");
            assertTrue(calls[0]);

            queueManager.onMessage(message -> {
                assertEquals(messageMaker.makeMessage("test4"), message);
                calls[1] = true;
            });
            queueManager.send("test4");
            assertTrue(calls[1]);
        }
    }

    @Test
    public void getClassesTest() {
        final List<QueueManager.ClassProperties> classProperties = queueManager.getClasses();
        assertEquals(1, classProperties.size());
        assertEquals(ConnectionFactoryTest.class, classProperties.get(0).clazz);
        assertEquals(2, classProperties.get(0).properties.size());
        final Iterator<String> iterator = classProperties.get(0).properties.stream().sorted(Comparator.naturalOrder()).iterator();
        assertEquals("value1", iterator.next());
        assertEquals("value2", iterator.next());
    }

    private static class MessageMaker {
        private final String value1;
        private final String value2;
        private final String queueIn;
        private final String queueOut;

        private MessageMaker(String value1, String value2, String queueIn, String queueOut) {
            this.value1 = value1;
            this.value2 = value2;
            this.queueIn = queueIn;
            this.queueOut = queueOut;
        }

        private String makeMessage(String message) {
            return "from " + queueIn + " to " + queueOut + " " + StringUtils.trimToEmpty(value1) + message + StringUtils.trimToEmpty(value2);
        }
    }

    public static class ConnectionFactoryTest implements ConnectionFactory {
        private String value1;
        private String value2;

        @Override
        public Connection createConnection() throws JMSException {
            return mockConnection();
        }

        @Override
        public Connection createConnection(String s, String s1) throws JMSException {
            return null;
        }

        @Override
        public JMSContext createContext() {
            return null;
        }

        @Override
        public JMSContext createContext(String s, String s1) {
            return null;
        }

        @Override
        public JMSContext createContext(String s, String s1, int i) {
            return null;
        }

        @Override
        public JMSContext createContext(int i) {
            return null;
        }

        public String getValue1() {
            return value1;
        }

        public void setValue1(String value1) {
            this.value1 = value1;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }


        private Connection mockConnection() throws JMSException {
            final Connection connection = mock(Connection.class);
            final Session session = mock(Session.class);
            doAnswer(invocation -> {
                final String message = (String) invocation.getArguments()[0];
                return mockMessage(value1, message, value2);
            }).when(session).createTextMessage(anyString());

            doAnswer(invocation -> {
                final String queueName = (String) invocation.getArguments()[0];
                final Queue queue = mock(Queue.class);
                when(queue.toString()).thenReturn(queueName);
                return queue;
            }).when(session).createQueue(anyString());

            final MessageConsumer consumer = mock(MessageConsumer.class);
            final MessageListener[] messageListeners = new MessageListener[]{null};
            doAnswer(invocation -> {
                messageListeners[0] = (MessageListener) invocation.getArguments()[0];
                return null;
            }).when(consumer).setMessageListener(any(MessageListener.class));

            final String[] queueOut = new String[]{null};
            doAnswer(invocation -> {
                queueOut[0] = invocation.getArguments()[0].toString();
                return consumer;
            }).when(session).createConsumer(any(Destination.class));

            doAnswer(invocation -> createProducer(invocation.getArguments()[0].toString(), queueOut, messageListeners)).when(session).createProducer(any(Destination.class));

            when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
            return connection;
        }

        private TextMessage mockMessage(String prefix, String message, String postfix) throws JMSException {
            final TextMessage textMessage = mock(TextMessage.class);
            when(textMessage.getText()).thenReturn(StringUtils.trimToEmpty(prefix) + message + StringUtils.trimToEmpty(postfix));
            return textMessage;
        }

        private MessageProducer createProducer(String queueIn, String[] queueOut, MessageListener[] messageListeners) throws JMSException {
            final MessageProducer producer = mock(MessageProducer.class);
            doAnswer(invocation -> {
                messageListeners[0].onMessage(mockMessage(null, "from " + queueIn + " to " + queueOut[0] + " " + ((TextMessage) invocation.getArguments()[0]).getText(), null));
                return null;
            }).when(producer).send(any(TextMessage.class));
            return producer;
        }
    }
}
