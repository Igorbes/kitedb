package pro.kitedb.dao;


import pro.kitedb.exception.DataException;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.Filter;

public interface DaoCount<O, F extends Filter<? super O>> extends KiteDao {
    Long count(DataGraph<O, ? super F> dataGraph) throws DataException;
}
