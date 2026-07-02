package pro.kitedb;

import pro.kitedb.dao.KiteDao;
import pro.kitedb.factory.context.Variable;
import pro.kitedb.factory.copy.CopyManager;
import pro.kitedb.graph.DataGraph;

public interface CopyJdbcTemplate extends KiteJdbc {
    default <T, F> void copy(String templateName, DataGraph<T, ? extends F> dataGraph, CopyManager copyManager) throws Exception {
        copy(templateName, dataGraph, copyManager, Variable.EMPTY);
    }
    <T, F> void copy(String templateName, DataGraph<T, ? extends F> objectGraph, CopyManager copyManager, Variable variableParams) throws Exception;
}
