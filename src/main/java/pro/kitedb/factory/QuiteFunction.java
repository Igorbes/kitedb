package pro.kitedb.factory;

public interface QuiteFunction<T, R> {
    R apply(T t) throws Exception;
}
