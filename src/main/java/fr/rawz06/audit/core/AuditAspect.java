package fr.rawz06.audit.core;

import fr.rawz06.audit.annotations.Audited;
import fr.rawz06.audit.annotations.AuditIgnore;
import fr.rawz06.audit.serializer.ArgumentSerializer;
import fr.rawz06.audit.serializer.MaskingArgumentSerializer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Method;

@Aspect
public class AuditAspect {
    private static final ArgumentSerializer serializer = new MaskingArgumentSerializer();

    @Around("@annotation(fr.rawz06.audit.annotations.Audited)")
    public Object auditMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        Audited audited = method.getAnnotation(Audited.class);
        return performAudit(joinPoint, method, audited);
    }

    @Around("@within(fr.rawz06.audit.annotations.Audited)")
    public Object auditClassCall(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        
        // Check if method has @AuditIgnore
        if (method.isAnnotationPresent(AuditIgnore.class)) {
            return joinPoint.proceed();
        }
        
        Audited audited = method.getAnnotation(Audited.class);
        if (audited == null) {
            audited = method.getDeclaringClass().getAnnotation(Audited.class);
        }
        
        return performAudit(joinPoint, method, audited);
    }

    private Object performAudit(ProceedingJoinPoint joinPoint, Method method, Audited audited) throws Throwable {
        if (audited == null) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        long duration = -1;

        try {
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = audited.action().isEmpty() ? method.getName() : audited.action();
            Object[] args = joinPoint.getArgs();

            String argsStr = serializeArguments(method, args, audited.mask());

            Object result;
            try {
                result = joinPoint.proceed();
            } catch (Throwable e) {
                status = "ERROR";
                throw e;
            } finally {
                try {
                    duration = System.currentTimeMillis() - startTime;
                } catch (Exception e) {
                    duration = -1;
                    System.err.println("[AUDIT-INTERNAL] Failed to calculate duration: " + e.getMessage());
                }

                emitAuditLog(className, methodName, argsStr, status, duration);
            }

            return result;
        } catch (Throwable e) {
            throw e;
        }
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }

    private String serializeArguments(Method method, Object[] args, String[] masks) {
        try {
            return serializer.serialize(method, args, masks);
        } catch (Exception e) {
            System.err.println("[AUDIT-INTERNAL] Failed to serialize arguments: " + e.getMessage());
            return "<serialization_failed>";
        }
    }

    private void emitAuditLog(String className, String methodName, String argsStr, String status, long durationMs) {
        try {
            String auditLine = AuditEventBuilder.build(className, methodName, argsStr, status, durationMs);
            System.out.println(auditLine);
        } catch (Exception e) {
            System.err.println("[AUDIT-INTERNAL] Failed to emit audit log: " + e.getMessage());
        }
    }
}
