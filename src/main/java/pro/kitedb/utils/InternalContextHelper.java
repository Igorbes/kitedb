package pro.kitedb.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.directive.Block;
import pro.kitedb.KiteJdbcTemplate;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class InternalContextHelper {
    private final KiteJdbcTemplate kiteJdbcTemplate;

    public InternalContextHelper(KiteJdbcTemplate kiteJdbcTemplate) {
        this.kiteJdbcTemplate = kiteJdbcTemplate;
    }

    public String queryForObject(String sql) {
        return DbUtils.toString(query(sql, new HashMap()));
    }

    private Object queryForObject(String sql, Map params) {
        return kiteJdbcTemplate.getJdbcTemplate().queryForObject(sql, params, Object.class);
    }

    public Object query(String sql, Map params) {
        return kiteJdbcTemplate.getJdbcTemplate().queryForObject(sql, params, Object.class);
    }

    public String queryInClause(String sql, Map params) {
        List<Object> list = kiteJdbcTemplate.getJdbcTemplate().queryForList(sql, params, Object.class);
        return list.stream().filter(v -> v != null).map(v -> DbUtils.toString(v)).collect(Collectors.joining(", "));
    }

    public String toArray(Collection<?> collection) {
        return collection.stream().filter(v -> v != null).map(v -> v.toString()).collect(Collectors.joining(", "));
    }

    public String toArray(Object[] array) {
        return toArray(Arrays.asList(array));
    }

    public List<Object> queryList(Block.Reference reference) {
        Map context = new HashMap();
        Context velocityContext = getVelocityContext(reference);
        Object[] keys = velocityContext.getKeys();
        if (!ArrayUtils.isEmpty(keys)) {
            for (Object key : keys) {
                context.put(key, velocityContext.get((String) key));
            }
        }
        return queryList(reference.toString(), context);
    }

    public List<Object> queryList(String sql, Map params) {
        return kiteJdbcTemplate.getJdbcTemplate().queryForList(sql, params, Object.class);
    }

    public Object query(Block.Reference reference) {
        Map context = new HashMap();
        Context velocityContext = getVelocityContext(reference);
        Object[] keys = velocityContext.getKeys();
        if (!ArrayUtils.isEmpty(keys)) {
            for (Object key : keys) {
                context.put(key, velocityContext.get((String) key));
            }
        }

        return query(reference.toString(), context);
    }

    public String queryInClause(Block.Reference reference) {
        Map context = new HashMap();
        Context velocityContext = getVelocityContext(reference);
        Object[] keys = velocityContext.getKeys();
        if (!ArrayUtils.isEmpty(keys)) {
            for (Object key : keys) {
                context.put(key, velocityContext.get((String) key));
            }
        }

        return queryInClause(reference.toString(), context);
    }

    public String in(Collection<Object> collection) {
        if (CollectionUtils.isEmpty(collection)) return "null";
        return collection.stream().map(v -> {
            if (v instanceof Number) {
                return v.toString();
            } else {
                return String.format("'%s'", v);
            }
        }).collect(Collectors.joining(", "));
    }

    private Context getVelocityContext(Block.Reference reference) {
        try {
            Field context = reference.getClass().getDeclaredField("context");
            context.setAccessible(true);
            return (Context) context.get(reference);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }
}
