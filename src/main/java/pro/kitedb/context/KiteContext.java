package pro.kitedb.context;

import pro.kitedb.KiteJdbcTemplate;
import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.utils.InternalContextHelper;

import java.util.Collection;
import java.util.Map;

public interface KiteContext {
    void put(KiteContext context);
    void put(Variable variable);
    void put(String key, Object value);
    void put(DataGraph<?, ?> graph);

    String optional(String sql) throws DataException;
    String optional(String sql, String mappedField) throws DataException;

    boolean hasVar(String key);
    Object var(String key);

    Map<String, Object> collectPreparedParams() throws DataException;
    DataGraph<?, ?> graph();
    boolean isJoined(String field) throws DataException;
    KiteContext join(String field) throws DataException;
    String joinKey(String field) throws DataException;
    String sql() throws DataException;

    boolean isCopy();
    boolean isInsert();
    boolean isSelect();
    boolean isDelete();
    boolean isUpdate();
    boolean isCount();
    boolean isExecute();

    Collection<Object> collection();
    ContextFilter filter() throws DataException;

    KiteJdbcTemplate jdbc();
    InternalContextHelper help();
    KiteContext parent();
    String end();
}
