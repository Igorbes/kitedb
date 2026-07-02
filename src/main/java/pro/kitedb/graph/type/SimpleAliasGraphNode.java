package pro.kitedb.graph.type;

import pro.kitedb.graph.Filter;
import pro.kitedb.exception.DataException;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.DataGraphBiConsumer;
import pro.kitedb.graph.prefix.SqlFieldPrefixFactory;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.simpleflatmapper.jdbc.JdbcMapperFactory;

import java.util.HashMap;
import java.util.Map;

public class SimpleAliasGraphNode<O, F extends Filter<? super O>> implements GraphNode<O, F> {
    private final String fieldName;
    private final SqlFieldPrefixFactory sqlFieldPrefixFactory;
    private final @Getter boolean isKey;
    private final @Getter DataGraphBiConsumer<DataGraph<O, F>, JdbcMapperFactory> discriminator;

    public SimpleAliasGraphNode(String fieldName, SqlFieldPrefixFactory sqlFieldPrefixFactory, boolean isKey) {
        this(fieldName, sqlFieldPrefixFactory, isKey, null);
    }

    public SimpleAliasGraphNode(String fieldName, SqlFieldPrefixFactory sqlFieldPrefixFactory, boolean isKey, DataGraphBiConsumer<DataGraph<O, F>, JdbcMapperFactory> discriminator) {
        this.fieldName = fieldName;
        this.sqlFieldPrefixFactory = sqlFieldPrefixFactory;
        this.isKey = isKey;
        this.discriminator = discriminator;
    }

    @Override
    public Map<? extends String, ? extends String> getAlias(DataGraph<O, F> parentGraph, boolean withPrefix) {
        Map<String, String> res = new HashMap<>();
        res.put(getAliasName(parentGraph, fieldName, withPrefix), fieldName);
        return res;
    }

    @Override
    public Map<? extends String, ? extends String> getAlias(DataGraph<O, F> parentGraph) {
        return getAlias(parentGraph, true);
    }

    @Override
    public String getAliasName(DataGraph<O, F> parentGraph, String field, boolean withPrefix) {
        String prefix = sqlFieldPrefixFactory.getPrefix();
        return withPrefix && StringUtils.isNotBlank(prefix) ? prefix + "." + field : field;
    }

    @Override
    public String getAliasName(DataGraph<O, F> parentGraph, String field) {
        return getAliasName(parentGraph, field, true);
    }

    @Override
    public String[] keys(DataGraph<O, F> parentGraph) {
        return keys(parentGraph, true);
    }

    @Override
    public String[] keys(DataGraph<O, F> parentGraph, boolean withPrefix) {
        return isKey ? new String[]{getAliasName(parentGraph, fieldName, withPrefix)} : null;
    }

    @Override
    public void discriminate(DataGraph<O, F> parentGraph, JdbcMapperFactory jdbcMapperFactory) throws DataException {
        if(discriminator != null) {
            discriminator.accept(parentGraph, jdbcMapperFactory);
        }
    }
}
