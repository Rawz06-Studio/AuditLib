package fr.rawz06.audit.serializer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public abstract class BaseArgumentSerializer implements ArgumentSerializer {
    protected static final int MAX_VALUE_LENGTH = 2000;

    protected String getParameterName(Parameter[] parameters, int index) {
        if (index < parameters.length) {
            try {
                String name = parameters[index].getName();
                if (name != null && !name.startsWith("arg")) {
                    return name;
                }
            } catch (Exception e) {
                // Fallback to argN
            }
        }
        return "arg" + index;
    }

    protected String serializeValue(Object value) {
        if (value == null) {
            return "null";
        }

        try {
            String str = value.toString();
            return str.length() > MAX_VALUE_LENGTH
                    ? str.substring(0, MAX_VALUE_LENGTH) + "..."
                    : str;
        } catch (Exception e) {
            return "<toString_failed>";
        }
    }
}
