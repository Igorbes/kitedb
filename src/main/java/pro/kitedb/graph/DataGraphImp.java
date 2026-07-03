package pro.kitedb.graph;

import pro.kitedb.exception.DataException;
import pro.kitedb.graph.prefix.SqlFieldPrefixFactory;
import pro.kitedb.graph.type.GraphNode;
import pro.kitedb.graph.type.SimpleAliasGraphNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.simpleflatmapper.jdbc.JdbcMapperFactory;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DataGraphImp<O, F extends Filter<? super O>> implements DataGraph<O, F> {
    private static final Logger LOG = LoggerFactory.getLogger(DataGraph.class);
    private static final String NO_ALIAS = "NO_ALIAS";

    DataGraphFun<DataGraph<O, F>, String> onJoinTable;
    final Map<String, GraphNode<O, F>> graphNodeMap;
    final SqlFieldPrefixFactory<O> sqlFieldPrefixFactory;
    final String sqlTablePrefix;
    F filter;

    DataGraphImp(SqlFieldPrefixFactory<O> sqlFieldPrefixFactory, String sqlTablePrefix, Map<String, GraphNode<O, F>> graphNodeMap, DataGraphFun<DataGraph<O, F>, String> onJoinTable) {
        this(sqlFieldPrefixFactory, sqlTablePrefix, graphNodeMap, onJoinTable, null);
    }

    DataGraphImp(SqlFieldPrefixFactory<O> sqlFieldPrefixFactory, String sqlTablePrefix, Map<String, GraphNode<O, F>> graphNodeMap, DataGraphFun<DataGraph<O, F>, String> onJoinTable, F filter) {
        this.graphNodeMap = graphNodeMap;
        this.onJoinTable = onJoinTable;
        this.sqlFieldPrefixFactory = sqlFieldPrefixFactory;
        this.filter = filter;
        this.sqlTablePrefix = sqlTablePrefix;
    }

    @Override
    public Map<String, String> aliases(boolean withPrefix) throws DataException {
        Map<String, String> res = new HashMap<>();

        for(String key: graphNodeMap.keySet()) {
            GraphNode graphNode = graphNodeMap.get(key);
            res.putAll(graphNode.getAlias(this, withPrefix));
        }
        return res;
    }


    @Override
    public Map<String, String> aliases() throws DataException {
        return aliases(true);
    }

    @Override
    public boolean required(String fieldName) {
        LOG.debug("Check required object models field: " + fieldName);
        return graphNodeMap.containsKey(fieldName);
    }

    @Override
    public String optional(String column, String field) throws DataException {
        if(!this.required(field)) return "";

        StringBuilder sb = new StringBuilder();
        if(this.sqlTablePrefix != null) {
            sb.append(this.sqlTablePrefix).append(".");
        }
        if(this.isDataGraph(field)) {
            sb.append("*").append(",");
        } else {
            String alias = this.alias(field);
            sb.append(column).append(" as ").append(alias).append(",");
        }
        return sb.toString();
    }

    @Override
    public String optional(String column) throws DataException {
        return optional(column, column);
    }

    @Override
    public GraphNode<O, F> get(String column) throws DataException {
        return graphNodeMap.get(column);
    }

    @Override
    public String alias(String field) throws DataException {
        GraphNode<O, F> graphNode = graphNodeMap.get(field);
        if(graphNode == null) return NO_ALIAS;

        if(graphNode instanceof SimpleAliasGraphNode) {
            String aliasName = graphNode.getAliasName(this, field);
            LOG.debug(String.format("Field %s mapped to %s", field, aliasName));
            return String.format("\"%s\"", aliasName);
        } else {
            throw new DataException("Field \"" + field + "\" have to been SimpleAliasGraphNode");
        }
    }

    @Override
    public boolean hasAlias(String field) throws DataException {
       return graphNodeMap.containsKey(field);
    }

    @Override
    public DataGraph<?, F> child(String fieldName) throws DataException {
        GraphNode<O, F> graphNode = graphNodeMap.get(fieldName);
        if(!(graphNode instanceof  DataJoinLayerGraphNode)) throw new DataException(String.format("Graph for alias \"%s\" have to been DataJoinLayerGraphNode", fieldName));

        DataJoinLayerGraphNode<?, F> dataJoinLayerGraphType = (DataJoinLayerGraphNode<?, F>) graphNode;
        DataGraphFun<DataGraph<?, ?>, ? extends DataGraph<?, F>> function = dataJoinLayerGraphType.getFunction();
        return function.apply(this);
    }

    @Override
    public String join() throws DataException {
        if(onJoinTable == null)  throw new RuntimeException("Unknown sql template. Prefix: " + this.prefix());
        return onJoinTable.apply(this);
    }

    @Override
    public JdbcTemplateMapperFactory mapperFactory() throws DataException {
        return JdbcTemplateMapperFactory.newInstance()
                .addAliases(aliases())
                .addKeys(keys());
    }

    @Override
    public String[] keys() {
        return keys(true);
    }

    @Override
    public String[] keys(boolean withPrefix) {
        String[] keys = graphNodeMap.values().stream()
                .map(v -> v.keys(this, withPrefix))
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .toArray(String[]::new);
        return keys;
    }

    @Override
    public String prefix() {
        return sqlFieldPrefixFactory.getPrefix();
    }

    @Override
    public Class<? super O> getObjectClass() {
        return sqlFieldPrefixFactory.getClazz();
    }


    @Override
    public F getFilter() {
        return filter;
    }

    @Override
    public void discriminator(JdbcMapperFactory jdbcMapperFactory) throws DataException {
        Set<String> fields = graphNodeMap.keySet();
        for(String field: fields) {
            graphNodeMap.get(field).discriminate(field, this, jdbcMapperFactory);
        }
    }

    @Override
    public DataGraphBuilder<O, F> toBuilder() {
        return DataGraphBuilder.<O, F>from(this);
    }

    @Override
    public DataGraph<O, F> table(String tableAlias) throws DataException {
        return this.toBuilder()
                .tablePrefix(tableAlias)
                .build();
    }

    @Override
    public boolean isDataGraph(String column) throws DataException {
        return graphNodeMap.get(column) instanceof DataJoinLayerGraphNode;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.writeValueAsString(DataGraphUtils.toMap(this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
