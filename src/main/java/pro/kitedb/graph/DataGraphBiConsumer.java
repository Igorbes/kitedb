package pro.kitedb.graph;

import pro.kitedb.exception.DataException;

public interface DataGraphBiConsumer<T, U> {
    void accept(T t, U u) throws DataException;
}
