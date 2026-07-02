package pro.kitedb;

import org.apache.commons.lang3.ArrayUtils;
import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.Filter;

public interface SelectJdbcTemplate extends KiteJdbc {
    default <T, F> T selectObject(String templateName, DataGraph<T, ? extends F> objectGraph) throws DataException {
        return selectObject(templateName, objectGraph, Variable.EMPTY);
    }
    default <T, F> T selectObject(String templateName, DataGraph<T, ? extends F> dataGraph, Variable variableParams) throws DataException {
        T[] result = select(templateName, dataGraph, variableParams);
        if(ArrayUtils.isEmpty(result)) return null;

        if(result.length > 1) throw new DataException("Result size more then one");
        return result[0];
    }
    default <T, F> T[] select(String templateName, DataGraph<T, ? extends F> dataGraph) throws DataException {
        return select(templateName, dataGraph, Variable.EMPTY);
    }
    <T, F> T[] select(String templateName, DataGraph<T, ? extends F> objectGraph, Variable variableParams) throws DataException;
}
