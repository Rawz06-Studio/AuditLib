package fr.rawz06.audit.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface Audited {
    /**
     * Action name for the audit log. If empty, uses method name.
     */
    String action() default "";

    /**
     * Regex patterns to mask parameter values. Matched against parameter names.
     * Matching parameter values are replaced with ***.
     */
    String[] mask() default {};
}
