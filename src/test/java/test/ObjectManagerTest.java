package test;

import com.dsid.model.ObjectManager;
import com.dsid.model.QueueManager;
import com.dsid.model.impl.ObjectManagerImpl;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ObjectManagerTest {
    final ObjectManager objectManager = new ObjectManagerImpl();

    @Test
    public void fullTest() throws MalformedURLException {
        {
            final Map<String, String> map = new HashMap<>();
            map.put("value1", "test1");
            map.put("value2", "test2");
            final TestClass testClass = objectManager.create(TestClass.class, TestClass2.class.getName(), map);
            final TestClass2 testClass2 = (TestClass2) testClass;
            assertEquals("test1", testClass2.getValue1());
            assertEquals("test2", testClass2.getValue2());
            assertEquals(2, testClass2.settersCount);
        }
        objectManager.register(new File("src/test/resources/testLoad.jar").toURI().toURL());
        {
            final Map<String, String> map = new HashMap<>();
            final QueueManager queueManager = objectManager.create(QueueManager.class, "ru.dsid.test.QueueManagerTest", map);
            final AtomicInteger messages = new AtomicInteger();
            queueManager.onMessage(message -> {
                messages.getAndIncrement();
                assertEquals("testMessage", message);
            });
            queueManager.send("testMessage");
            assertEquals(1, messages.get());
        }
        {
            final Map<String, String> map = new HashMap<>();
            final String prefix = "testPrefix";
            final String postfix = "testPostfix";
            map.put("prefix", prefix);
            map.put("postfix", postfix);
            final QueueManager queueManager = objectManager.create(QueueManager.class, "ru.dsid.test.QueueManagerTest", map);
            final AtomicInteger messages = new AtomicInteger();
            queueManager.onMessage(message -> {
                messages.getAndIncrement();
                assertEquals(prefix + "testMessage" + postfix, message);
            });
            queueManager.send("testMessage");
            assertEquals(1, messages.get());
        }
    }

    public static class TestClass {
        protected int settersCount = 0;
        private String value1;

        public void setValue1(String value1) {
            this.value1 = value1;
            settersCount++;
        }

        public String getValue1() {
            return value1;
        }
    }

    public static class TestClass2 extends TestClass {
        private String value2;

        public void setValue2(String value2) {
            this.value2 = value2;
            settersCount++;
        }

        public String getValue2() {
            return value2;
        }
    }

}