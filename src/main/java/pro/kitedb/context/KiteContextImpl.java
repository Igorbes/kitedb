package pro.kitedb.context;

import pro.kitedb.KiteJdbcTemplate;
import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.Filter;
import pro.kitedb.utils.InternalContextHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KiteContextImpl implements KiteContext {
    private Map<String, Object> contextMap = new HashMap<>();
    /* TODO: Need without ThreadLocal. Maybe create Tree */
    public final static ThreadLocal<Map<String, Object>> PREPARED_PARAMS = ThreadLocal.withInitial(HashMap::new);

    public KiteContextImpl(KiteJdbcTemplate jdbcTemplate) {
        contextMap.put("_jdbc", jdbcTemplate);
        contextMap.put("_help", new InternalContextHelper(jdbcTemplate));
    }

    @Override
    public void put(KiteContext context) {
        contextMap.put("_parent", context);
    }

    @Override
    public void put(Variable variable) {
        contextMap.putAll(variable.getContext());
    }

    @Override
    public Map<String, Object> collectPreparedParams() throws DataException {
        Map<String, Object> collected = PREPARED_PARAMS.get();
        Map<String, Object> res = new HashMap<>(collected);
        collected.clear();
        return res;
    }

    @Override
    public void put(String key, Object value) {
        contextMap.put(key, value);
    }

    @Override
    public void put(DataGraph<?, ?> graph) {
        contextMap.put("_graph", graph);
    }

    @Override
    public DataGraph<?, ?> graph() {
        return (DataGraph<?, ?>) contextMap.get("_graph");
    }

    @Override
    public boolean hasVar(String key) {
        return contextMap.containsKey(key);
    }

    @Override
    public Object var(String key) {
        Object var = contextMap.get(key);
        if(var instanceof String) {
            return String.format("'%s'", var);
        } else {
            return var != null ? var : "null";
        }
    }

    @Override
    public KiteJdbcTemplate jdbc() {
        return (KiteJdbcTemplate) contextMap.get("_jdbc");
    }

    @Override
    public InternalContextHelper help() {
        return (InternalContextHelper) contextMap.get("_help");
    }

    @Override
    public KiteContext parent() {
        return (KiteContext) contextMap.get("_parent");
    }

    @Override
    public String end() {
        return "'end'";
    }

    @Override
    public String optional(String sql) throws DataException {
        String[] split = sql.split("\\.");
        String column = split[1];
        return optional(sql, column);
    }

    @Override
    public String optional(String sql, String mappedField) throws DataException {
        String[] split = sql.split("\\.");
        String tableAlias = split[0];
        String column = split[1];
        return this.graph().table(tableAlias).optional(column, mappedField);
    }

    @Override
    public boolean isJoined(String field) {
        return this.graph().required(field);
    }

    @Override
    public KiteContext join(String field) throws DataException {
        DataGraph<?, ?> graph = this.graph();
        DataGraph<?, ?> child = graph.child(field);

        KiteContext context = new KiteContextImpl(this.jdbc());
        context.put(this);
        context.put(child);
        return context;
    }

    @Override
    public String sql() throws DataException {
        return this.graph().join();
    }

    @Override
    public String joinKey(String field) throws DataException {
        return this.graph().alias(field);
    }

    @Override
    public ContextFilter filter() throws DataException {
        Map<String, Object> variables = new HashMap<>(32);
        String prefix = "";
        DataGraph<?, ?> graph = this.graph();
        if(graph != null) {
            prefix = graph.prefix();
            Filter<?> filter = graph.getFilter();
            if(filter != null) {
                variables.putAll(filter.toParams());
            }
        }
        return new ContextFilterPreparedParam(variables, prefix);
    }

    @Override
    public boolean isCopy() {
        return contextMap.containsKey("_copy");
    }

    @Override
    public boolean isInsert() {
        return contextMap.containsKey("_insert");
    }

    @Override
    public boolean isSelect() {
        return contextMap.containsKey("_select");
    }

    @Override
    public boolean isDelete() {
        return contextMap.containsKey("_delete");
    }

    @Override
    public boolean isUpdate() {
        return contextMap.containsKey("_update");
    }

    @Override
    public boolean isCount() {
        return contextMap.containsKey("_count");
    }

    @Override
    public boolean isExecute() {
        return contextMap.containsKey("_execute");
    }

    @Override
    public Collection<Object> collection() {
        return (Collection<Object>) contextMap.get("_collection");
    }

}
