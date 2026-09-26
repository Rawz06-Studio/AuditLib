package fr.rawz06.audit.serializer;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultArgumentSerializerTest {

    private final ArgumentSerializer serializer = new DefaultArgumentSerializer();

    @Test
    void testNullArgs() throws Exception {
        Method method = TestClass.class.getMethod("noArgs");
        String result = serializer.serialize(method, null, null);
        assertEquals("", result);
    }

    @Test
    void testEmptyArgs() throws Exception {
        Method method = TestClass.class.getMethod("noArgs");
        String result = serializer.serialize(method, new Object[0], null);
        assertEquals("", result);
    }

    @Test
    void testSingleArg() throws Exception {
        Method method = TestClass.class.getMethod("singleArg", String.class);
        String result = serializer.serialize(method, new Object[]{"hello"}, null);
        assertTrue(result.contains("hello"));
    }

    @Test
    void testMultipleArgs() throws Exception {
        Method method = TestClass.class.getMethod("multipleArgs", String.class, int.class);
        String result = serializer.serialize(method, new Object[]{"test", 42}, null);
        assertTrue(result.contains("test"));
        assertTrue(result.contains("42"));
    }

    @Test
    void testNullValueHandling() throws Exception {
        Method method = TestClass.class.getMethod("singleArg", String.class);
        String result = serializer.serialize(method, new Object[]{null}, null);
        assertTrue(result.contains("null"));
    }

    @Test
    void testTruncationOfLongValues() throws Exception {
        Method method = TestClass.class.getMethod("singleArg", String.class);
        String longString = "x".repeat(3000);
        String result = serializer.serialize(method, new Object[]{longString}, null);
        assertTrue(result.contains("..."));
        assertTrue(result.length() < 3500);
    }

    static class TestClass {
        public void noArgs() {}
        public void singleArg(String arg) {}
        public void multipleArgs(String arg1, int arg2) {}
    }
}
