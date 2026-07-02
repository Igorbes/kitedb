package pro.kitedb.graph;

import pro.kitedb.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface Variabale<O> {

    default Map<String, Object> toParams() {
        Map<String, Object> context = new HashMap<>();
        Class<? extends Variabale> aClass = this.getClass();
        Collection<Field> declaredFields = ReflectionUtils.getFieldDeep(aClass);
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            Collection<String> names = ReflectionUtils.getNames(declaredField);
            Object value = ReflectionUtils.getValue(this, declaredField);

            for (String name : names) {
                if(value == null) continue;

                context.put(name, value);
            }
        }
        return context;
    }
}
