package fr.rawz06.audit.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditEventBuilderTest {

    @Test
    void testBuildSuccessEvent() {
        String event = AuditEventBuilder.build("com.example.Service", "transfer", "accountId=123", "SUCCESS", 45);
        assertEquals("AUDIT|com.example.Service#transfer|accountId=123|SUCCESS|45", event);
    }

    @Test
    void testBuildErrorEvent() {
        String event = AuditEventBuilder.build("com.example.Auth", "login", "username=john", "ERROR", 12);
        assertEquals("AUDIT|com.example.Auth#login|username=john|ERROR|12", event);
    }

    @Test
    void testBuildWithMultipleArgs() {
        String event = AuditEventBuilder.build("com.example.Transfer", "process", "from=acc1, to=acc2, amount=5000", "SUCCESS", 156);
        assertTrue(event.contains("from=acc1, to=acc2, amount=5000"));
        assertTrue(event.startsWith("AUDIT|"));
        assertTrue(event.contains("|SUCCESS|"));
    }

    @Test
    void testBuildWithNegativeDuration() {
        String event = AuditEventBuilder.build("com.example.Query", "fetch", "userId=abc", "SUCCESS", -1);
        assertEquals("AUDIT|com.example.Query#fetch|userId=abc|SUCCESS|-1", event);
    }

    @Test
    void testBuildWithEmptyArgs() {
        String event = AuditEventBuilder.build("com.example.Util", "count", "", "SUCCESS", 5);
        assertEquals("AUDIT|com.example.Util#count||SUCCESS|5", event);
    }

    @Test
    void testFormatHasCorrectSeparator() {
        String event = AuditEventBuilder.build("com.example.Service", "test", "arg=val", "SUCCESS", 10);
        String[] parts = event.split("\\|");
        assertEquals(5, parts.length);
        assertEquals("AUDIT", parts[0]);
    }
}
