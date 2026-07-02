package pro.kitedb.factory;

public interface Factory {
    <T> T build(Class<T> tClass) throws Exception;
}
