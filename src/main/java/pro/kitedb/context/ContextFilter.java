package pro.kitedb.context;

import java.util.Map;

public interface ContextFilter {
    boolean present(String key);
    Object param(String key);
    Map<String, Object> getAll();
}
