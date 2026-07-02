package pro.kitedb;

import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.factory.returning.Returning;
import pro.kitedb.factory.returning.ReturningByColumnIndexNumber;
import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;

import java.util.*;

public interface InsertJdbcTemplate extends KiteJdbc {
    default <O> Number insert(String templateName, O t, KiteParameterizedPreparedStatementSetter<O> setter) throws DataException {
        return insert(templateName, Variable.EMPTY, t, setter);
    }
    default <O> Number insert(String templateName, Variable variableParams, O t, KiteParameterizedPreparedStatementSetter<O> setter) throws DataException {
        return insert(templateName, new ReturningByColumnIndexNumber(1), variableParams, t, setter);
    }
    default <O> Number[] insert(String templateName, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t) throws DataException {
        return insert(templateName, new ReturningByColumnIndexNumber(1), Variable.EMPTY, setter, t);
    }
    default <O> Number[] insert(String templateName, Variable variableParams, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t) throws DataException {
        return insert(templateName, new ReturningByColumnIndexNumber(1), variableParams, setter, t);
    }

    default <R> R insert(String templateName, Returning<R> returning, Variable variableParams) throws DataException {
        return insert(templateName, returning, variableParams, null);
    }
    default <R, O> R insert(String templateName, Returning<R> returning, Variable variableParams, O t) throws DataException {
        return insert(templateName, returning, variableParams, t, null);
    }
    default <R, O> R insert(String templateName, Returning<R> returning, Variable variableParams, O t, KiteParameterizedPreparedStatementSetter<O> setter) throws DataException {
        return insert(templateName, returning, variableParams, setter, Arrays.asList(t))[0];
    }
    default <R, O> R[] insert(String templateName, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t) throws DataException {
        return insert(templateName, returning, Variable.EMPTY, setter, t);
    }
    default <R, O> R[] insert(String templateName, Returning<R> returning, Variable variableParams, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t) throws DataException {
        return insert(templateName, returning, variableParams, setter, t, 1);
    }
    default <R, O> R[] insert(String templateName, Returning<R> returning, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t, int batchSize) throws DataException {
        return insert(templateName, returning, Variable.EMPTY, setter, t, batchSize);
    }
    <R, O> R[] insert(String templateName, Returning<R> returning, Variable variableParams, KiteParameterizedPreparedStatementSetter<O> setter, Collection<O> t, int batchSize) throws DataException;
}
