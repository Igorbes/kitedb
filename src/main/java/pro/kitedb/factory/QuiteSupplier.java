package pro.kitedb.factory;

public interface QuiteSupplier<T> {
    T get() throws Exception;
}
