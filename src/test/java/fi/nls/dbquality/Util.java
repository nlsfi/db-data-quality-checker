package fi.nls.dbquality;

import org.springframework.util.ReflectionUtils;

public class Util {
    public static void setField(String fieldName, Class<?> clazz, Object object, Object value) {
        var field = ReflectionUtils.findField(clazz, fieldName, null);
        field.setAccessible(true);
        ReflectionUtils.setField(field, object, value);
    }
}
