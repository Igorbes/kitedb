package pro.kitedb.graph.discriminator;

import pro.kitedb.graph.*;
import pro.kitedb.exception.DataException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.simpleflatmapper.converter.ContextualConverter;
import org.simpleflatmapper.map.property.ConverterProperty;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Discriminators {

    public static <O, F extends Filter<? super O>, T extends Enum<T>> Discrimination<O, F> discriminateToSet(Class<T> enumClass) {
        return (fieldName, dataGraph1, jdbcMapperFactory) -> {
            String columnName = dataGraph1.alias(fieldName).replace("\"", "");
            jdbcMapperFactory.addColumnProperty(
                    columnName,
                    ConverterProperty.of((ContextualConverter<Array, Set<?>>) (o, context) -> toSet(o, enumClass))
            );
        };
    }

    public static <O, F extends Filter<? super O>> Discrimination<O, F> discriminateToMap() {
        ObjectMapper objectMapper = new ObjectMapper();
        return (fieldName, dataGraph1, jdbcMapperFactory) -> {
            String columnName = (String) dataGraph1.alias(fieldName).replace("\"", "");
            jdbcMapperFactory.addColumnProperty(
                    columnName,
                    ConverterProperty.of((ContextualConverter<String, Map<String, ?>>) (o, context) -> toMap(objectMapper, o))
            );
        };
    }

    public static <O, F extends Filter<? super O>> Discrimination<O, F> discriminateToList() {
        return discriminateToList( o -> o);
    }

    public static <O, F extends Filter<? super O>> Discrimination<O, F> discriminateToList(Function<Object, ?> mapFun) {
        return (fieldName, dataGraph1, jdbcMapperFactory) -> {
            String columnName = (String) dataGraph1.alias(fieldName).replace("\"", "");
            jdbcMapperFactory.addColumnProperty(
                    columnName,
                    ConverterProperty.of((ContextualConverter<Array, List<?>>) (o, context) -> toList(o, mapFun))
            );
        };
    }

    public static <O, F extends Filter<? super O>> Discrimination<O, F> discriminateToArray() {
        return (filedName, dataGraph1, jdbcMapperFactory) -> {
            String columnName = dataGraph1.alias(filedName).replace("\"", "");
            jdbcMapperFactory.addColumnProperty(columnName, ConverterProperty.of((ContextualConverter<Array, ?>) (o, context) -> toArray(o)));
        };
    }

    public static <O, F extends Filter<? super O>> Discrimination<O, F> discriminateToFunction(Function<Object, ?> function) {
        return (filedName, dataGraph1, jdbcMapperFactory) -> {
            String columnName = dataGraph1.alias(filedName).replace("\"", "");
            jdbcMapperFactory.addColumnProperty(
                    columnName,
                    ConverterProperty.of((ContextualConverter<?, ?>) (o, context) -> function.apply(o))
            );
        };
    }

    public static <T extends Enum<T>, O, F extends Filter<? super O>> Discrimination<O, F> discriminateToEnumArray(Class<T> enumClass) {
        return (fieldName, dataGraph1, jdbcMapperFactory) -> {
            String columnName = dataGraph1.alias(fieldName).replace("\"", "");
            jdbcMapperFactory.addColumnProperty(
                    columnName,
                    ConverterProperty.of((ContextualConverter<Array, ?>) (o, context) -> toArray(o, enumClass))
            );
        };
    }

    public static <O, F extends Filter<? super O>> Discrimination<O, F> discriminateToLocalDate() {
        return (fieldName, dataGraph1, jdbcMapperFactory) -> {
            String columnName = dataGraph1.alias(fieldName).replace("\"", "");
            jdbcMapperFactory.addColumnProperty(
                    columnName,
                    ConverterProperty.of((ContextualConverter<Timestamp, ?>) (o, context) -> o  != null ? o.toLocalDateTime() : null)
            );
        };
    }

    public static <O, F extends Filter<? super O>> Discrimination<O, F> discriminateToZonedDateTime() {
        return (fieldName, dataGraph1, jdbcMapperFactory) -> {
            String columnName = dataGraph1.alias(fieldName).replace("\"", "");
            jdbcMapperFactory.addColumnProperty(
                    columnName,
                    ConverterProperty.of((ContextualConverter<Timestamp, ?>) (o, context) -> o  != null ? ZonedDateTime.of(o.toLocalDateTime(), ZoneId.systemDefault()) : null)
            );
        };
    }


    private static  <T> Set<T> toSet(Array o) throws SQLException {
        if(o != null) {
            T[] array = (T[]) o.getArray();
            if(ArrayUtils.isNotEmpty(array)) {
                return Arrays.asList(array).stream().filter(Objects::nonNull).collect(Collectors.toSet());
            }
        }
        return null;
    }
    private static Map<String, String> toMap(ObjectMapper objectMapper, String o) {
        try {
            return o != null ? objectMapper.readValue(o, HashMap.class) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private static  <T extends Enum<T>> T[] toArray(Array o, Class<T> enumClass) throws SQLException {
        if(o != null) {
            String[] array = (String[]) o.getArray();
            List<T> collection = Arrays.stream(array).map(v -> Enum.valueOf(enumClass, v)).collect(Collectors.toList());

            int length = Optional.ofNullable(collection).map(Collection::size).orElse(0);
            T[] ts = (T[]) java.lang.reflect.Array.newInstance(enumClass, length);
            return collection.toArray(ts);
        }
        return null;
    }
    private static  <T> T[] toArray(Array o) throws SQLException {
        if(o != null) {
            return (T[]) o.getArray();
        }
        return null;
    }
    private static  <T> List<T> toList(Array o) throws SQLException {
        return toList(o, o1 -> (T) o1);
    }
    private static  <T> List<T> toList(Array o, Function<Object, T> mapFun) throws SQLException {
        if(o != null) {
            T[] array = (T[]) o.getArray();
            if(ArrayUtils.isNotEmpty(array)) {
                return Arrays.asList(array).stream().filter(Objects::nonNull).map(mapFun::apply).collect(Collectors.toList());
            }
        }
        return null;
    }
    private static  <T extends Enum<T>> Set<T> toSet(Array o, Class<T> enumClass) throws SQLException {
        if(o != null) {
            String[] array = (String[]) o.getArray();
            return Arrays.stream(array).map(v -> Enum.valueOf(enumClass, v)).collect(Collectors.toSet());
        }
        return null;
    }

    public static  <T> T getNullableValue(SqlRowSet sqlRowSet, String field, Class<T> aClass) {
        return (T) sqlRowSet.getObject(field);
    }

    public static <T> T getNullableValue(SqlRowSet sqlRowSet, DataGraph<?, ?> dataGraph, String field, Class<T> aClass) throws DataException {
        if(dataGraph.required(field)) {
            String alias = (String) dataGraph.alias(field);
            alias = alias.substring(1, alias.length() - 1);
            return (T) sqlRowSet.getObject(alias);
        }
        return null;
    }

}
