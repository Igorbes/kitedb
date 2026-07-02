package pro.kitedb;

import org.apache.commons.lang3.ArrayUtils;
import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.factory.returning.Returning;
import pro.kitedb.factory.returning.ReturningByColumnIndexNumber;
import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;

import java.util.Collection;

public interface DeleteJdbcTemplate extends KiteJdbc {
    default Number[] delete(String templateName) throws DataException {
        return delete(templateName, Variable.EMPTY);
    }
    default Number[] delete(String templateName, Variable variableParams) throws DataException {
        return delete(templateName, variableParams, new ReturningByColumnIndexNumber(1));
    }

    default <R> R[] delete(String templateName, Variable variableParams, Returning<R> returning) throws DataException {
        return delete(templateName, variableParams, returning, null, null);
    }
    default <R, O> R[] delete(String templateName, Variable variableParams, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t) throws DataException {
        return delete(templateName, variableParams, returning, setter, t, 1);
    }
    default <R, O> R[] delete(String templateName, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t, int batchSize) throws DataException {
        return delete(templateName, Variable.EMPTY, returning, setter, t, batchSize);
    }
    <R, O> R[] delete(String templateName, Variable variableParams, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t, int batchSize) throws DataException;
}
