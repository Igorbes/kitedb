package pro.kitedb.graph;

import pro.kitedb.exception.DataException;
import org.simpleflatmapper.jdbc.JdbcMapperFactory;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import pro.kitedb.graph.type.GraphNode;

import java.util.Map;

public interface DataGraph<O, F extends Filter<? super O>> {
    Map<String, String> aliases() throws DataException;
    Map<String, String> aliases(boolean withPrefix) throws DataException;
    boolean required(String field);
    String optional(String column, String field) throws DataException;
    String optional(String column) throws DataException;
    String alias(String field) throws DataException;
    GraphNode<O, F> get(String field) throws DataException;
    boolean hasAlias(String field) throws DataException;
    DataGraph<?, F> child(String fieldName) throws DataException;
    String join() throws DataException;
    JdbcTemplateMapperFactory mapperFactory() throws DataException;
    String[] keys();
    String[] keys(boolean withPrefix);
    String prefix();
    Class<? super O> getObjectClass();
    F getFilter();
    void discriminator(JdbcMapperFactory jdbcMapperFactory) throws DataException;
    DataGraphBuilder<O, F> toBuilder();
    DataGraph<O, F> table(String tableAlias) throws DataException;
    boolean isDataGraph(String column) throws DataException;
}
