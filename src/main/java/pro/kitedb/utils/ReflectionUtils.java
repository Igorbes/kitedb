package pro.kitedb.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ReflectionUtils {
    private static final String REGEX_SNAKE_CASE = "([a-z])([A-Z]+)";

    public static Collection<Field> getFieldDeep(Class clazz) {
        return getFieldDeep(clazz, new ArrayList<>(32));
    }

    public static Collection<Field> getFieldDeep(Class clazz, Collection<Field> fields) {
        Field[] declaredFields = clazz.getDeclaredFields();
        if(ArrayUtils.isNotEmpty(declaredFields)) {
            fields.addAll(Arrays.asList(declaredFields));
        }
        Class superclass = clazz.getSuperclass();
        if(superclass != null) {
            return getFieldDeep(superclass, fields);
        } else {
            return fields;
        }
    }

    public static Collection<String> getNames(Field declaredField) {
        declaredField.setAccessible(true);
        String name = declaredField.getName();
        String replacement = "$1_$2";
        String nameSake = name.replaceAll(REGEX_SNAKE_CASE, replacement).toLowerCase();
        return Arrays.asList(nameSake, nameSake);
    }


    public static <O> Object getValue(Object object, Field declaredField) {
        declaredField.setAccessible(true);
        try {
            return declaredField.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <O> Object getValue(Object object) {
        if(object == null) return null;

        if(object instanceof Enum) {
            return ((Enum<?>) object).name();
        } else if(object instanceof Collection) {
            Collection res = new ArrayList(32);
            Collection collection = (Collection) object;
            for (Object o : collection) {
                res.add(getValue(o));
            }
            return res;
        } else if(object.getClass().isArray()) {
            int length = Array.getLength(object);
            Collection<Object> res = new ArrayList<>();
            for(int i = 0; i < length; i++)
                res.add(getValue(Array.get(object, i)));
            return res;
        }else {
            return object;
        }
    }

}
