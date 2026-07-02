package pro.kitedb.graph.prefix;

public interface SqlFieldPrefixFactory<O> {
    String getPrefix();
    Class<? super O> getClazz();
    void setPrefix(String prefix);
    void setClazz(Class<? super O> clazz);
}
