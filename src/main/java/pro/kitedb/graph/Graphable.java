package pro.kitedb.graph;


import pro.kitedb.exception.DataException;

public interface Graphable<O, F extends Filter<? super O>> {
    DataGraph<O, ? super F> graph(F filter) throws DataException;
}
