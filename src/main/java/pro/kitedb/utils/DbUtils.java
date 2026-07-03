package pro.kitedb.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.*;
import java.time.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DbUtils {

    public static Array toArray(PreparedStatement ps, Collection<? extends Object> v, String type) {
        if(CollectionUtils.isEmpty(v)) return null;

        Object[] arr = v.stream().toArray(Object[]::new);
        return toArray(ps, arr, type);
    }

    public static Array toArray(PreparedStatement ps, Object[] array, String type) {
        try {
            if(ArrayUtils.isEmpty(array)) return null;

            return ps.getConnection().createArrayOf(type, array);
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    public static String toString(Object v) {
        if(v instanceof String) {
            return String.format("'%s'", v);
        } if(v instanceof Date) {
            return String.format("'%s'", v.toString());
        } else {
            return v.toString();
        }
    }

    public static Timestamp toTimestamp(LocalDateTime ldt) {
        return new Timestamp(ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public static Timestamp toTimestamp(ZonedDateTime zonedDateTime) {
        return new Timestamp(zonedDateTime.toInstant().toEpochMilli());
    }

    public static Date toDate(LocalDate expired) {
        return new Date(expired.toEpochDay() * 24 * 60 * 60 * 1000);
    }
}
