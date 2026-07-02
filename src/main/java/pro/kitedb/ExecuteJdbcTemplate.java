package pro.kitedb;

import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;

public interface ExecuteJdbcTemplate extends KiteJdbc {
    default void execute(String templateName) throws DataException {
        execute(templateName, Variable.EMPTY);
    }
    void execute(String templateName, Variable variableParams) throws DataException;
}
