package pro.kitedb.graph;

import pro.kitedb.exception.DataException;

import java.util.Map;

public interface JoinTemplate {
    <O, F extends Filter<? super O>> String getJoinTemplate(DataGraph<O, F> subgraph) throws DataException;
}
