package pro.kitedb;

import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.factory.returning.Returning;
import pro.kitedb.factory.returning.ReturningByColumnIndexNumber;
import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;

import java.util.*;

public interface UpdateJdbcTemplate extends KiteJdbc {
    default Number update(String templateName) throws DataException {
        return update(templateName, Variable.EMPTY);
    }
    default Number update(String templateName, Variable variable) throws DataException {
        return update(templateName, variable, new ReturningByColumnIndexNumber(1)).length;
    }
    default <R, O> R[] update(String templateName, Variable variable, Returning<R> returning) throws DataException {
        return update(templateName, variable, returning, (KiteParameterizedPreparedStatementSetter<O>)null, (Collection<O>)null);
    }
    default <R, O> R[] update(String templateName, Variable variable, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, O t) throws DataException {
        return update(templateName, variable, returning, setter, Arrays.asList(t));
    }
    default <R, O> R[] update(String templateName, Variable variableParams, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t) throws DataException {
        return update(templateName, variableParams, returning, setter, t, 1);
    }

    default <R, O> R[] update(String templateName, Returning<R> returning) throws DataException {
        return update(templateName, Variable.EMPTY, returning);
    }
    default <R, O> R[] update(String templateName, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t) throws DataException {
        return update(templateName, Variable.EMPTY, returning, setter, t, 1);
    }
    default <R, O> R[] update(String templateName, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t, int batchSize) throws DataException {
        return update(templateName, Variable.EMPTY, returning, setter, t, batchSize);
    }

    <R, O> R[] update(String templateName, Variable variableParams, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t, int batchSize) throws DataException;
}
