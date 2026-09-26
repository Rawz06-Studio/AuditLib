package fr.rawz06.audit.serializer;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskingArgumentSerializerTest {

    private final ArgumentSerializer serializer = new MaskingArgumentSerializer();

    @Test
    void testNoMask() throws Exception {
        Method method = TestClass.class.getMethod("login", String.class, String.class);
        String result = serializer.serialize(method, new Object[]{"user1", "secret123"}, null);
        assertTrue(result.contains("secret123"));
    }

    @Test
    void testSimpleMask() throws Exception {
        Method method = TestClass.class.getMethod("login", String.class, String.class);
        String result = serializer.serialize(method, new Object[]{"user1", "secret123"}, new String[]{"password"});
        assertContainsMaskedValue(result, "password");
    }

    @Test
    void testRegexMask() throws Exception {
        Method method = TestClass.class.getMethod("login", String.class, String.class);
        String result = serializer.serialize(method, new Object[]{"user1", "secret123"}, new String[]{"pass.*"});
        assertContainsMaskedValue(result, "password");
    }

    @Test
    void testMultipleMasks() throws Exception {
        Method method = TestClass.class.getMethod("transfer", String.class, String.class, String.class);
        String result = serializer.serialize(method, new Object[]{"user1", "token123", "pwd"}, 
                new String[]{"token", "password"});
        assertTrue(result.contains("user1"));
        assertTrue(result.contains("***"));
    }

    @Test
    void testEmptyMask() throws Exception {
        Method method = TestClass.class.getMethod("login", String.class, String.class);
        String result = serializer.serialize(method, new Object[]{"user1", "secret123"}, new String[]{});
        assertTrue(result.contains("secret123"));
    }

    private void assertContainsMaskedValue(String result, String paramName) {
        assertTrue(result.contains("***"), "Expected *** in result for param: " + paramName);
    }

    static class TestClass {
        public void login(String username, String password) {}
        public void transfer(String username, String token, String password) {}
    }
}
