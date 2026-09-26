package fr.rawz06.audit.serializer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.regex.Pattern;

public class MaskingArgumentSerializer extends BaseArgumentSerializer {

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
            String value = shouldMask(paramName, masks)
                    ? "***"
                    : serializeValue(args[i]);

            sb.append(paramName).append("=").append(value);
        }

        return sb.toString();
    }

    private boolean shouldMask(String paramName, String[] masks) {
        if (masks == null) {
            return false;
        }

        for (String mask : masks) {
            try {
                if (Pattern.matches(mask, paramName)) {
                    return true;
                }
            } catch (Exception e) {
                // Invalid regex, skip
            }
        }
        return false;
    }
}
