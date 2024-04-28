package common.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
	public static <T> T parse(String jsonString, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        T obj = clazz.newInstance();
        Map<String, String> keyValueMap = extractKeyValuePairs(jsonString);

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (keyValueMap.containsKey(fieldName)) {
                String value = keyValueMap.get(fieldName);
                setFieldValue(obj, field, value);
            }
        }
        return obj;
    }

    private static  <T> void setFieldValue(T obj, Field field, String value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType == String.class) {
            field.set(obj, value);
        } else if (fieldType == int.class || fieldType == Integer.class) {
            field.set(obj, Integer.parseInt(value));
        }
    }

    private static Map<String, String> extractKeyValuePairs(String jsonString) {
        Map<String, String> keyValueMap = new HashMap<>();
        jsonString = jsonString.substring(1, jsonString.length() - 1); // Remove curly braces
        String[] pairs = jsonString.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].trim().replaceAll("\"", ""); // Remove quotes and trim
            String value = keyValue[1].trim().replaceAll("\"", ""); // Remove quotes and trim
            keyValueMap.put(key, value);
        }
        return keyValueMap;
    }
}
