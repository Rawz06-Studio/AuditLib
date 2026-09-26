package fr.rawz06.audit.serializer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface ArgumentSerializer {
    /**
     * Serialize method arguments into a readable string.
     *
     * @param method the method being invoked
     * @param args   the method arguments
     * @param masks  regex patterns to mask parameter names
     * @return serialized arguments string
     */
    String serialize(Method method, Object[] args, String[] masks);
}
