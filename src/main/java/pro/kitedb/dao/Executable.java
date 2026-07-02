package pro.kitedb.dao;


import pro.kitedb.factory.QuiteSupplier;
import pro.kitedb.exception.DataException;

public interface Executable extends KiteDao {
    <T> T execute(QuiteSupplier<T> supplier) throws DataException;
}
