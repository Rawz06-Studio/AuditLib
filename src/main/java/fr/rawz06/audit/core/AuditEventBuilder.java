package fr.rawz06.audit.core;

public class AuditEventBuilder {
    private static final String SEPARATOR = "|";

    /**
     * Build an audit event line according to the template:
     * AUDIT|<full.class.name>#<methodName>|<arg1=val1, arg2=val2>|<STATUS>|<duration_ms>
     *
     * @param fullClassName full class name with package
     * @param methodName    method name
     * @param argsStr       serialized arguments
     * @param status        SUCCESS or ERROR
     * @param durationMs    duration in milliseconds (-1 if unknown)
     * @return formatted audit line
     */
    public static String build(String fullClassName, String methodName, String argsStr, String status, long durationMs) {
        return "AUDIT" +
                SEPARATOR +
                fullClassName + "#" + methodName +
                SEPARATOR +
                argsStr +
                SEPARATOR +
                status +
                SEPARATOR +
                durationMs;
    }
}
