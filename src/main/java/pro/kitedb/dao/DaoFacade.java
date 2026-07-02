package pro.kitedb.dao;

import pro.kitedb.factory.QuiteSupplier;
import pro.kitedb.exception.DataException;
import pro.kitedb.factory.copy.CopyManager;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.Filter;

import java.io.OutputStream;

public abstract class DaoFacade<I, O, F extends Filter<? super O>> implements DaoFullAccess<I, O, F> {

    public abstract DaoFullAccess<I, O, F> getImplementation();

    @Override
    public void copy(DataGraph<O, F> dataGraph, CopyManager cp) throws Exception {
        getImplementation().copy(dataGraph, cp);
    }

    @Override
    public Long count(DataGraph<O, ? super F> dataGraph) throws DataException {
        return getImplementation().count(dataGraph);
    }

    @Override
    public <T> T execute(QuiteSupplier<T> supplier) throws DataException {
        return getImplementation().execute(supplier);
    }

    @Override
    public O[] select(DataGraph<O, ? super F> dataGraph) throws DataException {
        return getImplementation().select(dataGraph);
    }

    @Override
    public DataGraph<O, ? super F> graph(F filter) throws DataException {
        return getImplementation().graph(filter);
    }

    @Override
    public I[] delete(I... models) throws DataException {
        return getImplementation().delete(models);
    }

    @Override
    public I[] update(O... models) throws DataException {
        return getImplementation().update(models);
    }

    @Override
    public I[] insert(O... models) throws DataException {
        return getImplementation().insert(models);
    }
}
