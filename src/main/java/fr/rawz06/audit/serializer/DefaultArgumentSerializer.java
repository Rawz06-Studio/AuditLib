package fr.rawz06.audit.serializer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class DefaultArgumentSerializer extends BaseArgumentSerializer {

    @Override
    public String serialize(Method method, Object[] args, String[] masks) {
        if (args == null || args.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            String paramName = getParameterName(parameters, i);
            String value = serializeValue(args[i]);

            sb.append(paramName).append("=").append(value);
        }

        return sb.toString();
    }
}
