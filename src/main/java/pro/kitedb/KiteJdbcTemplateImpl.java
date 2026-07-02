package pro.kitedb;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.simpleflatmapper.jdbc.JdbcMapperFactory;
import org.simpleflatmapper.jdbc.spring.ResultSetExtractorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.lang.Nullable;
import pro.kitedb.context.KiteContext;
import pro.kitedb.context.KiteContextImpl;
import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.factory.copy.CopyManager;
import pro.kitedb.factory.returning.Returning;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.Filter;
import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;
import pro.kitedb.utils.DbUtils;
import pro.kitedb.utils.KiteUtils;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class KiteJdbcTemplateImpl implements KiteJdbcTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(KiteJdbcTemplate.class);

    private final NamedParameterJdbcTemplate templateJdbcTemplate;

    public KiteJdbcTemplateImpl(DataSource dataSource) {
        this.templateJdbcTemplate = new KiteNamedParameterJdbcTemplate(dataSource);
    }

    public void init() {
        LOG.debug("Kite db initialization resources");
        Properties p = new Properties();
        p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        p.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init( p );
    }

    @Override
    public <T, F> T[] select(String templateName, DataGraph<T, ? extends F> objectGraph, Variable variable) throws DataException {
        LOG.debug("Select data from graph: " + objectGraph + " with template: " + templateName + " with context: " + variable);
        JdbcMapperFactory jdbcMapperFactory = JdbcMapperFactory.newInstance()
                .addAliases(objectGraph.aliases())
                .addKeys(objectGraph.keys())
                .unorderedJoin()
                .useAsm(false)
                .ignorePropertyNotFound();
        objectGraph.discriminator(jdbcMapperFactory);

        KiteContext context = new KiteContextImpl(this);
        context.put(variable);
        context.put(objectGraph);
        context.put("_select", true);

        Class<? super T> objectClass = objectGraph.getObjectClass();
        String sql = KiteUtils.merge(templateName, context);
        LOG.debug("SQL: " + sql);
        List<T> result = templateJdbcTemplate.query(sql, context.collectPreparedParams(), new ResultSetExtractorImpl<T>(jdbcMapperFactory.newMapper((Type) objectClass)));
        T[] rs = (T[]) Array.newInstance(objectClass, result.size());
        for (int i = 0; i < result.size(); i++) {
            rs[i] = result.get(i);
        }
        return rs;
    }

    @Override
    public <R, O> R[] insert(String templateName, Returning<R> returning, Variable variable, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t, int batchSize) throws DataException {
        return run("_insert", templateName, returning, variable, setter, t, batchSize);
    }

    @Override
    public <R, O> R[] delete(String templateName, Variable variable, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t, int batchSize) throws DataException {
        return run("_delete", templateName, returning, variable, setter, t, batchSize);
    }

    @Override
    public <R, O> R[] update(String templateName, Variable variable, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t, int batchSize) throws DataException {
        return run("_update", templateName, returning, variable, setter, t, batchSize);
    }

    private  <R, O> R[] run(String operation, String templateName, Returning<R> returning, Variable variable, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> collection, int batchSize) throws DataException {
        try {
            LOG.debug("Update data with template: " + templateName + " with variable: " + variable + " with returning: " + returning);

            KiteContext context = new KiteContextImpl(this);
            context.put(variable);
            context.put(operation, true);
            context.put("_collection", collection);

            SqlParameterSource sqlParameterSource = new MapSqlParameterSource(variable.getContext());
            JdbcTemplate jdbcTemplate = templateJdbcTemplate.getJdbcTemplate();

            List<R> result = new ArrayList<>(64);

            List<Object> namedParams = new ArrayList<>();
            PreparedStatementCreator psc = connection -> {
                String sql = KiteUtils.merge(templateName, context);
                LOG.debug("SQL: " + sql);
                ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
                List<SqlParameter> sqlParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, sqlParameterSource);
                if (CollectionUtils.isNotEmpty(sqlParameters)) {
                    Object[] objects = NamedParameterUtils.buildValueArray(parsedSql, sqlParameterSource, null);
                    if (ArrayUtils.isNotEmpty(objects)) {
                        namedParams.addAll(Arrays.asList(objects));
                    }
                }
                String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, sqlParameterSource);
                return connection.prepareStatement(sqlToUse, Statement.RETURN_GENERATED_KEYS);
            };
            if(CollectionUtils.isEmpty(collection)) {
                PreparedStatementCallback<List<R>> pscall = preparedStatement -> {
                    if (CollectionUtils.isNotEmpty(namedParams)) {
                        AtomicInteger counter = new AtomicInteger(0);
                        for (Object param : namedParams) {
                            if (param instanceof Collection) {
                                Collection<Object> param1 = (Collection<Object>) param;
                                for (Object object : param1) {
                                    preparedStatement.setObject(counter.incrementAndGet(), object);
                                }
                            } else {
                                preparedStatement.setObject(counter.incrementAndGet(), param);
                            }
                        }
                    }
                    preparedStatement.addBatch();
                    preparedStatement.executeBatch();
                    ResultSet resultSet = preparedStatement.getGeneratedKeys();
                    List<R> ids = new ArrayList<>();
                    while (resultSet.next()) {
                        try {
                            ids.add(returning.returning(resultSet));
                        } catch (Exception e) {
                            throw new DataException(e);
                        }
                    }
                    return ids;
                };
                List<R> batchResult = jdbcTemplate.execute(psc, pscall);
                result.addAll(batchResult);
            } else {
                List<List<O>> batchParts = Lists.partition(new ArrayList<>(collection), Math.min(batchSize, collection.size()));
                for (List<O> batchPart : batchParts) {
                    PreparedStatementCallback<List<R>> pscall = preparedStatement -> {
                        if (CollectionUtils.isNotEmpty(namedParams)) {
                            AtomicInteger counter = new AtomicInteger(0);
                            for (Object param : namedParams) {
                                if (param instanceof Collection) {
                                    Collection<Object> param1 = (Collection<Object>) param;
                                    for (Object object : param1) {
                                        preparedStatement.setObject(counter.incrementAndGet(), object);
                                    }
                                } else {
                                    preparedStatement.setObject(counter.incrementAndGet(), param);
                                }
                            }
                        }

                        if (setter != null && CollectionUtils.isNotEmpty(batchPart)) {
                            for (O o : batchPart) {
                                AtomicInteger atomicInteger = new AtomicInteger(0);
                                setter.setValues(preparedStatement, atomicInteger, o);
                                preparedStatement.addBatch();
                            }
                        } else {
                            preparedStatement.addBatch();
                        }

                        preparedStatement.executeBatch();
                        ResultSet resultSet = preparedStatement.getGeneratedKeys();
                        List<R> ids = new ArrayList<>();
                        while (resultSet.next()) {
                            try {
                                ids.add(returning.returning(resultSet));
                            } catch (Exception e) {
                                throw new DataException(e);
                            }
                        }
                        return ids;
                    };
                    List<R> batchResult = jdbcTemplate.execute(psc, pscall);
                    result.addAll(batchResult);
                }
            }
            R[] rs = (R[]) Array.newInstance(returning.getReturnClass(), result.size());
            for (int i = 0; i < result.size(); i++) {
                rs[i] = result.get(i);
            }
            return rs;
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    @Override
    public <T, F extends Filter<T>> Long count(String templateName, DataGraph<T, ? extends F> objectGraph, Variable variable) throws DataException {
        LOG.debug("Count data from graph: " + objectGraph + " with template: " + templateName + " with context: " + variable);

        KiteContext context = new KiteContextImpl(this);
        context.put(objectGraph);
        context.put(variable);
        context.put("_count", true);

        String sql = KiteUtils.merge(templateName, context);
        LOG.debug("SQL: " + sql);
        Long result = templateJdbcTemplate.queryForObject(sql, context.collectPreparedParams(), Long.class);
        return result != null ? result : 0L;
    }

    @Override
    public NamedParameterJdbcTemplate getJdbcTemplate() {
        return this.templateJdbcTemplate;
    }

    @Override
    public void execute(String templateName, Variable variableParams) throws DataException {
        LOG.debug("Execute: " + templateName + " with context: " + variableParams);
        KiteContext context = new KiteContextImpl(this);
        context.put("_execute", true);
        String sql = KiteUtils.merge(templateName, context);
        LOG.debug("SQL: " + sql);
        templateJdbcTemplate.update(sql, context.collectPreparedParams());
    }

    @Override
    public <T, F> void copy(String templateName, DataGraph<T, ? extends F> objectGraph, CopyManager copyManager, Variable variable) throws Exception {
        LOG.debug("Copy data with template: " + templateName + " with graph: " + objectGraph + " with context: " + variable);
        KiteContext context = new KiteContextImpl(this);
        context.put(objectGraph);
        context.put(variable);
        context.put("_copy", true);

        String sql = KiteUtils.merge(templateName, context);
        LOG.debug("SQL: " + sql);

        DataSource dataSource = this.getJdbcTemplate().getJdbcTemplate().getDataSource();
        Connection connection = dataSource.getConnection();
        KiteNamedParameterJdbcTemplate namedParameterJdbcTemplateExt = new KiteNamedParameterJdbcTemplate(dataSource);
        PreparedStatementCreator preparedStatementCreator = namedParameterJdbcTemplateExt.getPreparedStatementCreator(sql, new MapSqlParameterSource(context.collectPreparedParams()));
        PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection);
        try {
            Statement pgStatement = preparedStatement.unwrap(Statement.class);
            String preparedSql= pgStatement.toString();
            copyManager.copyOut(connection, preparedSql);
        } finally {
            try {
                preparedStatement.close();
            } catch (Exception e) {}
            try {
                connection.close();
            } catch (Exception e) {}
        }
    }

    class KiteNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {
        public KiteNamedParameterJdbcTemplate(DataSource dataSource) {
            super(dataSource);
        }

        public KiteNamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
            super(classicJdbcTemplate);
        }

        @Override
        public PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
            return super.getPreparedStatementCreator(sql, paramSource);
        }
    }
}
