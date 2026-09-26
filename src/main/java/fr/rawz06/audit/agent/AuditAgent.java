package fr.rawz06.audit.agent;

import fr.rawz06.audit.annotations.Audited;
import fr.rawz06.audit.annotations.AuditIgnore;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

public class AuditAgent {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        new AgentBuilder.Default()
                .type(ElementMatchers.isAnnotatedWith(Audited.class)
                        .or(ElementMatchers.hasSuperType(ElementMatchers.isAnnotatedWith(Audited.class))))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.method(ElementMatchers.isPublic()
                                .and(ElementMatchers.not(ElementMatchers.isAnnotatedWith(AuditIgnore.class)))
                                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("$")))
                                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("<"))))
                                .intercept(Advice.to(AuditAdvice.class))
                )
                .installOn(instrumentation);

        System.out.println("[AuditAgent] Installed. Watching for @Audited methods...");
    }

    public static class AuditAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.This Object instance,
                @Advice.Origin Method method,
                @Advice.AllArguments Object[] args,
                @Advice.Local("auditStartTime") long startTime
        ) {
            startTime = System.currentTimeMillis();
        }

        @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
        public static void onExit(
                @Advice.This Object instance,
                @Advice.Origin Method method,
                @Advice.AllArguments Object[] args,
                @Advice.Return(readOnly = false) Object returned,
                @Advice.Thrown Throwable thrown,
                @Advice.Local("auditStartTime") long startTime
        ) {
            try {
                long duration = System.currentTimeMillis() - startTime;
                String argsStr = serializeArgs(method, args);
                String status = thrown == null ? "SUCCESS" : "ERROR";
                String className = method.getDeclaringClass().getName();
                String methodName = method.getName();

                String auditLine = fr.rawz06.audit.core.AuditEventBuilder.build(
                        className, methodName, argsStr, status, duration
                );
                System.out.println(auditLine);
            } catch (Throwable e) {
                System.err.println("[AUDIT-INTERNAL] Advice failed: " + e.getMessage());
            }
        }

        private static String serializeArgs(Method method, Object[] args) {
            try {
                fr.rawz06.audit.serializer.ArgumentSerializer ser = 
                        new fr.rawz06.audit.serializer.MaskingArgumentSerializer();
                Audited audited = method.getAnnotation(Audited.class);
                if (audited == null) {
                    audited = method.getDeclaringClass().getAnnotation(Audited.class);
                }
                String[] masks = audited != null ? audited.mask() : new String[0];
                return ser.serialize(method, args, masks);
            } catch (Exception e) {
                System.err.println("[AUDIT-INTERNAL] Serialization failed: " + e.getMessage());
                return "<serialization_failed>";
            }
        }
    }
}
