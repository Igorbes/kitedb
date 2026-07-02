package pro.kitedb.factory;

import org.apache.commons.lang3.ArrayUtils;
import pro.kitedb.KiteJdbcTemplateImpl;
import pro.kitedb.dao.KiteDao;
import pro.kitedb.dao.annotation.*;
import pro.kitedb.factory.context.ArrayVariable;
import pro.kitedb.factory.copy.CopyManager;
import pro.kitedb.graph.Filter;
import pro.kitedb.graph.Graphable;
import pro.kitedb.KiteJdbcTemplate;
import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;
import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.factory.returning.Returning;
import pro.kitedb.graph.DataGraph;
import com.google.common.collect.Lists;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class KiteDB {
    private final KiteJdbcTemplate jdbcTemplate;
    private final Factory factory;

    public KiteDB(DataSource dataSource, Factory factory) {
        this.jdbcTemplate = new KiteJdbcTemplateImpl(dataSource);
        this.factory = factory;
    }

    public KiteDB(DataSource dataSource) {
        this(dataSource, new Factory() {
            @Override
            public <T> T build(Class<T> tClass) throws Exception {
                return tClass.newInstance();
            }
        });
    }

    public <T extends KiteDao> T create(Class<T> clazz) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(KiteDB.class.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    String name = method.getName();
                    switch (name) {
                        case "insert":
                            return operate(proxy, method, args, clazz, (arg) -> insert((Object[]) arg, clazz));
                        case "update":
                            return operate(proxy, method, args, clazz, (arg) -> update((Object[]) arg, clazz));
                        case "delete":
                            return operate(proxy, method, args, clazz, (arg) -> delete((Object[]) arg, clazz));
                        case "select":
                            return select(proxy, method, args, clazz);
                        case "selectForObject":
                            return selectForObject(proxy, method, args, clazz);
                        case "count":
                            return count(proxy, method, args, clazz);
                        case "execute":
                            return execute(proxy, method, args, clazz);
                        case "graph":
                            return graph(proxy, method, args, clazz);
                        case "copy":
                            return copy(proxy, method, args, clazz);
                        default:
                            throw new DataException("Unreached code");
                    }
                } catch (Exception e) {
                    throw new DataException(e);
                }
            }
        });
    }

    private <T, R> R execute(Object proxy, Method method, Object[] args, Class<T> clazz) throws Exception {
        QuiteSupplier<R> supplier = (QuiteSupplier<R>) args[0];
        return supplier.get();
    }

    private <T, R> R operate(Object proxy, Method method, Object[] args, Class<T> daoClass, QuiteFunction<Object, Object[]> function) throws Exception {
        Object arg = args[0];
        if (arg.getClass().isArray()) {
            return (R) function.apply((Object[]) arg);
        } else {
            R[] result = (R[]) function.apply(new Object[]{arg});
            return result.length == 1 ? result[0] : null;
        }
    }

    private <T, R> R[] update(Object[] data, Class<T> daoClass) throws Exception {
        DaoUpdateAbility annotation = daoClass.getAnnotation(DaoUpdateAbility.class);
        if (annotation == null) throw new DataException("DaoInsertAbility annotation does not found");

        return update(Arrays.stream(data).collect(Collectors.toList()), annotation);
    }

    private <T, R> R[] delete(Object[] data, Class<T> daoClass) throws Exception {
        DaoDeleteAbility annotation = daoClass.getAnnotation(DaoDeleteAbility.class);
        if (annotation == null) throw new DataException("DaoInsertAbility annotation does not found");

        return delete(Arrays.stream(data).collect(Collectors.toList()), annotation);
    }

    private <R> R[] update(Collection collection, DaoUpdateAbility annotation) throws Exception {
        String template = annotation.template();
        int batchSize = annotation.batchSize();
        KiteParameterizedPreparedStatementSetter<?> parameterizedPreparedStatementSetter = factory.build(annotation.ppss());
        Class<? extends Returning<?>> returnClass = annotation.returning();
        Returning<?> returning = returnClass.newInstance();
        return (R[]) jdbcTemplate.update(template, returning, parameterizedPreparedStatementSetter, collection, batchSize);
    }

    private <T, R> R[] insert(Object[] arguments, Class<T> daoClass) throws Exception {
        DaoInsertInheritAbility insertInheritMetaData = daoClass.getAnnotation(DaoInsertInheritAbility.class);
        if (insertInheritMetaData != null) {
            Class<? extends Returning<?>> returningClass = insertInheritMetaData.returning();
            Returning<?> returning = returningClass.newInstance();
            R[] rs = (R[]) Array.newInstance(returning.getReturnClass(), 0);
            Map<Class, Collection<Object>> map = Arrays.stream(arguments).collect(Collectors.toMap(Object::getClass, Arrays::asList, (objects1, objects2) -> Arrays.asList(objects1, objects2).stream().flatMap(v -> v.stream()).collect(Collectors.toList())));
            for (DaoInsertAbility daoInsertAbility : insertInheritMetaData.meta()) {
                Collection<Object> collections = map.get(daoInsertAbility.clazz());
                rs = ArrayUtils.addAll(rs, insert(collections, daoInsertAbility));
            }
            return rs;
        } else {
            DaoInsertAbility annotation = daoClass.getAnnotation(DaoInsertAbility.class);
            if (annotation == null) throw new DataException("DaoInsertAbility annotation does not found");

            return insert(Arrays.stream(arguments).collect(Collectors.toList()), annotation);
        }
    }

    private <R> R[] insert(Collection collection, DaoInsertAbility annotation) throws Exception {
        String template = annotation.template();
        int batchSize = annotation.batchSize();
        KiteParameterizedPreparedStatementSetter<?> parameterizedPreparedStatementSetter = factory.build(annotation.ppss());
        Class<? extends Returning<?>> returnClass = annotation.returning();
        Returning<?> returning = returnClass.newInstance();
        return (R[]) jdbcTemplate.insert(template, returning, parameterizedPreparedStatementSetter, collection, batchSize);
    }

    private <R> R[] delete(Collection collection, DaoDeleteAbility annotation) throws Exception {
        String template = annotation.template();
        int batchSize = annotation.batchSize();
        Class<? extends Returning<R>> returnClass = (Class<? extends Returning<R>>) annotation.returning();
        Returning<R> returning = returnClass.newInstance();
        KiteParameterizedPreparedStatementSetter<R> parameterizedPreparedStatementSetter = (KiteParameterizedPreparedStatementSetter<R>) factory.build(annotation.ppss());
        return (R[]) jdbcTemplate.delete(template, returning, parameterizedPreparedStatementSetter, collection, batchSize);
    }

    private <T, R> R count(Object proxy, Method method, Object[] args, Class<T> clazz) throws Exception {
        DaoSelectAbility annotation = clazz.getAnnotation(DaoSelectAbility.class);
        String template = annotation.template();
        DataGraph dataGraph = (DataGraph) args[0];
        return (R) jdbcTemplate.count(template, dataGraph, Variable.EMPTY);
    }

    private <O> O[] select(Object proxy, Method method, Object[] args, Class clazz) throws Exception {
        DaoSelectAbility annotation = (DaoSelectAbility) clazz.getAnnotation(DaoSelectAbility.class);
        if (annotation == null) throw new DataException("GetMetaData annotation does not found");

        String template = annotation.template();
        DataGraph<O, ?> dataGraph = (DataGraph) args[0];
        return jdbcTemplate.select(template, dataGraph);
    }

    private <R> R selectForObject(Object proxy, Method method, Object[] args, Class clazz) throws Exception {
        R[] result = select(proxy, method, args, clazz);
        if (ArrayUtils.isEmpty(result)) return null;

        if (result.length > 1)
            throw new DataException(String.format("Incorrect result. Expect 1 actual %s", result.length));
        return result[0];
    }

    private <T, X extends DataGraph<T, Filter<T>>> X graph(Object proxy, Method method, Object[] args, Class<T> clazz) throws Exception {
        DaoSelectAbility annotation = clazz.getAnnotation(DaoSelectAbility.class);
        if (annotation == null) throw new DataException("GetMetaData annotation does not found");

        Class<Graphable<T, Filter<T>>> graphableClass = (Class<Graphable<T, Filter<T>>>) annotation.graphable();
        Graphable<T, Filter<T>> graphable = factory.build(graphableClass);
        return (X) method.invoke(graphable, args);
    }

    private <T, R> R copy(Object proxy, Method method, Object[] args, Class<T> clazz) throws Exception {
        DaoCopyAbility annotation = clazz.getAnnotation(DaoCopyAbility.class);
        if (annotation == null) throw new DataException("CopyMetaData annotation does not found");

        DataGraph<T, ?> dataGraph = (DataGraph<T, ?>) args[0];
        CopyManager copyManager = (CopyManager) args[1];

        String template = annotation.template();
        jdbcTemplate.copy(template, dataGraph, copyManager);
        return null;
    }
}
