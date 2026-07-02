package pro.kitedb.graph.reader;

import pro.kitedb.graph.DataGraphUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


public class PlainObjectGraph implements ClassGraphReader {
    private final Class<?> aClass;
    private final String field;
    private final String prefix;
    private final BiFunction<List<String>, Class<?>, Boolean> filter;

    public PlainObjectGraph(String prefix, Class<?> aClass, String field) {
        this(prefix, aClass, field, (strings, aClass1) -> true);
    }

    public PlainObjectGraph(Class<?> aClass, String field) {
        this("", aClass, field, (strings, aClass1) -> true);
    }

    public PlainObjectGraph(String prefix, Class<?> aClass, String field, BiFunction<List<String>, Class<?>, Boolean> filter) {
        this.aClass = aClass;
        this.field = field;
        this.prefix = prefix;
        this.filter = filter;
    }


    public List<String> getGraph() {
        List<String> result = new ArrayList<>();
        result.add(field);
        try {
            String[] split = field.split("\\.");
            if(split.length != 0) {
                List<List<String>> res = new ArrayList<>();
                goOverFields(res, new ArrayList<>(), this.aClass);

                String ast = split[split.length - 1];
                List<String> joinedField = res.stream().map(v -> v.stream().collect(Collectors.joining("."))).collect(Collectors.toList());
                String pattern = Arrays.stream(Arrays.copyOfRange(split, 0, split.length - 1)).collect(Collectors.joining("."));
                if("*".equals(ast)) {
                    joinedField = res.stream()
                            .map(v -> v.toArray(new String[v.size()]))
                            .filter(v -> v.length == split.length)
                            .filter(v -> Arrays.stream(Arrays.copyOfRange(v, 0, v.length - 1)).collect(Collectors.joining(".")).startsWith(pattern))
                            .map( v -> Arrays.stream(v).collect(Collectors.joining(".")))
                            .collect(Collectors.toList());
                } else {
                    joinedField = joinedField.stream().map(v -> split[0] + "." + v).collect(Collectors.toList());
                }
                result.addAll(joinedField);
            }
            return result.stream().map(v -> prefix + v).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void goOverFields(List<List<String>> res, List<String> parentSb, Class<?> aClass) {
        if(!DataGraphUtils.isSimpleValueType(aClass)) {
            Field[] declaredFields = aClass.getDeclaredFields();
            if(declaredFields.length != 0) {
                for(Field field: declaredFields) {
                    boolean isTransient = Modifier.isTransient(field.getModifiers());
                    boolean isStatic = Modifier.isStatic(field.getModifiers());
                    if(isTransient || isStatic) continue;

                    List<String> sb = new ArrayList<>(parentSb);
                    sb.add(field.getName());
                    goOverFields(res, sb, field.getType());
                }
            }
        } else {
            res.add(parentSb);
        }
    }
}
