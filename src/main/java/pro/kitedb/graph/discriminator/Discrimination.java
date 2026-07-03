package pro.kitedb.graph.discriminator;

import org.simpleflatmapper.jdbc.JdbcMapperFactory;
import pro.kitedb.exception.DataException;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.Filter;

public interface Discrimination<O, F extends Filter<? super O>> {
    void accept(String field, DataGraph<O, F> dataGraph, JdbcMapperFactory mapperFactory) throws DataException;
}
