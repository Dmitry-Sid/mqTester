package test;

import com.dsid.model.ObjectConverter;
import com.dsid.model.impl.ObjectConverterImpl;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObjectConverterTest {
    private final ObjectConverter objectConverter = new ObjectConverterImpl();

    @Test
    public void fullTest() {
        final String fileName = "testClass.temp";
        objectConverter.toFile(new TestClass("val1", "val2"), fileName);
        final TestClass testClass = objectConverter.fromFile(TestClass.class, fileName);
        assertEquals(testClass.value1, "val1");
        assertEquals(testClass.value2, "val2");
        assertTrue(new File(fileName).delete());
    }

    private static class TestClass implements Serializable {
        private final String value1;
        private final String value2;

        private TestClass(String value1, String value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public String getValue1() {
            return value1;
        }

        public String getValue2() {
            return value2;
        }
    }
}
