package fr.rawz06.audit.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AuditIgnore {
    /**
     * Exclude a method from audit when its class is annotated with @Audited.
     */
}
