package pro.kitedb.dao;

import pro.kitedb.factory.copy.CopyManager;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.Filter;

public interface DaoCopyable<O, F extends Filter<? super O>> extends KiteDao {
    void copy(DataGraph<O, F> dataGraph, CopyManager copyManager) throws Exception;
}
