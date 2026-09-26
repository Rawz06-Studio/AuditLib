package fr.rawz06.audit.core;

import fr.rawz06.audit.annotations.Audited;
import fr.rawz06.audit.serializer.ArgumentSerializer;
import fr.rawz06.audit.serializer.MaskingArgumentSerializer;

import java.lang.reflect.Method;

public class AuditAspect {
    private static final ArgumentSerializer serializer = new MaskingArgumentSerializer();

    /**
     * Intercept method invocation and emit audit log.
     * This method is thread-safe and never throws an exception that would break the calling code.
     *
     * @param method      the method being invoked
     * @param instance    the instance (may be null for static methods)
     * @param args        the method arguments
     * @param invocation  lambda to invoke the actual method
     * @return the result of the method invocation
     */
    public static Object audit(Method method, Object instance, Object[] args, AuditInvocation invocation) {
        Audited audited = method.getAnnotation(Audited.class);
        if (audited == null && instance != null) {
            audited = instance.getClass().getAnnotation(Audited.class);
        }

        if (audited == null) {
            // Not annotated, just invoke
            try {
                return invocation.invoke();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        long duration = -1;

        try {
            // Serialize arguments with masking
            String argsStr = serializeArguments(method, args, audited.mask());
            String action = audited.action().isEmpty() ? method.getName() : audited.action();
            String fullClassName = method.getDeclaringClass().getName();

            Object result;
            try {
                result = invocation.invoke();
            } catch (Exception e) {
                status = "ERROR";
                throw e;
            } finally {
                try {
                    duration = System.currentTimeMillis() - startTime;
                } catch (Exception e) {
                    System.err.println("[AUDIT-INTERNAL] Failed to calculate duration: " + e.getMessage());
                }

                emitAuditLog(fullClassName, action, argsStr, status, duration);
            }

            return result;
        } catch (Exception e) {
            // Ensure we always throw the original exception
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    private static String serializeArguments(Method method, Object[] args, String[] masks) {
        try {
            return serializer.serialize(method, args, masks);
        } catch (Exception e) {
            System.err.println("[AUDIT-INTERNAL] Failed to serialize arguments: " + e.getMessage());
            return "<serialization_failed>";
        }
    }

    private static void emitAuditLog(String className, String methodName, String argsStr, String status, long durationMs) {
        try {
            String auditLine = AuditEventBuilder.build(className, methodName, argsStr, status, durationMs);
            System.out.println(auditLine);
        } catch (Exception e) {
            System.err.println("[AUDIT-INTERNAL] Failed to emit audit log: " + e.getMessage());
        }
    }

    @FunctionalInterface
    public interface AuditInvocation {
        Object invoke() throws Exception;
    }
}
