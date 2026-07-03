package pro.kitedb.graph.type;

import pro.kitedb.graph.Filter;
import pro.kitedb.exception.DataException;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.discriminator.Discrimination;
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
    private final @Getter Discrimination<O, F> discrimination;

    public SimpleAliasGraphNode(String fieldName, SqlFieldPrefixFactory sqlFieldPrefixFactory, boolean isKey) {
        this(fieldName, sqlFieldPrefixFactory, isKey, null);
    }

    public SimpleAliasGraphNode(String fieldName, SqlFieldPrefixFactory sqlFieldPrefixFactory, boolean isKey, Discrimination<O, F> discrimination) {
        this.fieldName = fieldName;
        this.sqlFieldPrefixFactory = sqlFieldPrefixFactory;
        this.isKey = isKey;
        this.discrimination = discrimination;
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
    public void discriminate(String fieldName, DataGraph<O, F> parentGraph, JdbcMapperFactory jdbcMapperFactory) throws DataException {
        if(discrimination != null) {
            discrimination.accept(fieldName, parentGraph, jdbcMapperFactory);
        }
    }
}
