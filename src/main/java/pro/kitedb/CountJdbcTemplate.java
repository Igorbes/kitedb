package pro.kitedb;

import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.factory.returning.Returning;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.Filter;
import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;

import java.util.Collection;

public interface CountJdbcTemplate extends KiteJdbc {
    default Long count(String templateName) throws DataException {
        return count(templateName, Variable.EMPTY);
    }
    default Long count(String templateName, Variable variableParams) throws DataException {
        return count(templateName, null, variableParams);
    }
    default <T, F extends Filter<T>> Long count(String templateName, DataGraph<T, ? extends F> objectGraph) throws DataException {
        return count(templateName, objectGraph, Variable.EMPTY);
    }
    <T, F extends Filter<T>> Long count(String templateName, DataGraph<T, ? extends F> objectGraph, Variable variableParams) throws DataException;
}
