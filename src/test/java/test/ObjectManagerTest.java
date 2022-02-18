package test;

import com.dsid.model.ObjectManager;
import com.dsid.model.QueueManager;
import com.dsid.model.impl.ObjectConverterImpl;
import com.dsid.model.impl.ObjectManagerImpl;
import com.dsid.model.impl.QueueManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ObjectManagerTest {
    private final ObjectManager objectManager = new ObjectManagerImpl(new ObjectConverterImpl(), "test");

    @Before
    public void before() {
        new File("test").delete();
    }

    @After
    public void after() {
        new File("test").delete();
    }

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

    @Test
    public void getSuccessorsTest() throws MalformedURLException {
        {
            final Set<Class<? extends TestClass>> set = objectManager.getSuccessors(TestClass.class);
            assertEquals(1, set.size());
            assertEquals(TestClass2.class, set.iterator().next());
        }
        {
            final Set<Class<? extends TestClass2>> set = objectManager.getSuccessors(TestClass2.class);
            assertEquals(0, set.size());
        }
        objectManager.register(new File("src/test/resources/testLoad.jar").toURI().toURL());
        {
            final Set<Class<? extends QueueManager>> set = objectManager.getSuccessors(QueueManager.class)
                    .stream().sorted(Comparator.comparing(Class::getName)).collect(Collectors.toCollection(LinkedHashSet::new));
            assertEquals(2, set.size());
            final Iterator<Class<? extends QueueManager>> iterator = set.iterator();
            assertEquals(QueueManagerImpl.class, iterator.next());
            assertEquals("ru.dsid.test.QueueManagerTest", iterator.next().getName());
        }
        // Должно второй раз заугрузиться бстрее
        {
            final Set<Class<? extends QueueManager>> set = objectManager.getSuccessors(QueueManager.class)
                    .stream().sorted(Comparator.comparing(Class::getName)).collect(Collectors.toCollection(LinkedHashSet::new));
            assertEquals(2, set.size());
            final Iterator<Class<? extends QueueManager>> iterator = set.iterator();
            assertEquals(QueueManagerImpl.class, iterator.next());
            assertEquals("ru.dsid.test.QueueManagerTest", iterator.next().getName());
        }
    }

    @Test
    public void getPropertiesTest() {
        {
            final Set<String> properties = objectManager.getProperties(TestClass.class)
                    .stream().sorted(Comparator.naturalOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
            assertEquals(1, properties.size());
            assertEquals("value1", properties.iterator().next());
        }
        {
            final Set<String> properties = objectManager.getProperties(TestClass2.class)
                    .stream().sorted(Comparator.naturalOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
            assertEquals(2, properties.size());
            final Iterator<String> iterator = properties.iterator();
            assertEquals("value1", iterator.next());
            assertEquals("value2", iterator.next());
        }
    }

    public static class TestClass {
        protected int settersCount = 0;
        private String value1;

        public String getValue1() {
            return value1;
        }

        public void setValue1(String value1) {
            this.value1 = value1;
            settersCount++;
        }
    }

    public static class TestClass2 extends TestClass {
        private String value2;

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
            settersCount++;
        }
    }

}