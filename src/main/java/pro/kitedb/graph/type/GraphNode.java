package pro.kitedb.graph.type;

import pro.kitedb.graph.Filter;
import pro.kitedb.exception.DataException;
import pro.kitedb.graph.DataGraph;
import org.simpleflatmapper.jdbc.JdbcMapperFactory;

import java.util.Map;

public interface GraphNode<O, F extends Filter<? super O>> {
    Map<? extends String,? extends String> getAlias(DataGraph<O, F> parentGraph, boolean withPrefix) throws DataException;

    Map<? extends String,? extends String> getAlias(DataGraph<O, F> parentGraph) throws DataException;

    String getAliasName(DataGraph<O, F> parentGraph, String field) throws DataException;

    String getAliasName(DataGraph<O, F> parentGraph, String field, boolean withPrefix) throws DataException;

    String[] keys(DataGraph<O, F> parentGraph) throws DataException;

    String[] keys(DataGraph<O, F> parentGraph, boolean withPrefix);

    void discriminate(DataGraph<O, F> parentGraph, JdbcMapperFactory jdbcMapperFactory) throws DataException;

    boolean isKey();
}
