package pro.kitedb.graph;

import pro.kitedb.exception.DataException;

public interface DataGraphFun<T, R> {
    R apply(T t) throws DataException;
}
