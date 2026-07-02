package pro.kitedb.context;

import java.util.Map;

public class ContextFilterImpl implements ContextFilter {
    private final Map<String, Object> parameters;

    public ContextFilterImpl(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean present(String key) {
        return parameters.containsKey(key);
    }

    @Override
    public Object param(String key) {
        return parameters.get(key);
    }

    @Override
    public Map<String, Object> getAll() {
        return parameters;
    }
}
