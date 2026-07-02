package pro.kitedb.context;

import pro.kitedb.graph.DataGraph;

import java.util.HashMap;
import java.util.Map;

public class ContextFilterPreparedParam extends ContextFilterImpl {
    private final String prefix;

    public ContextFilterPreparedParam(Map<String, Object> parameters, String prefix) {
        super(parameters);
        this.prefix = prefix;
    }

    @Override
    public String param(String key) {
        String preparedKey = prefix + "_" + key;
        Object value = super.param(key);
        KiteContextImpl.PREPARED_PARAMS.get().put(preparedKey, value);
        return ":" + preparedKey;
    }

    @Override
    public Map<String, Object> getAll() {
        Map<String, Object> all = super.getAll();
        Map<String, Object> params = new HashMap<>(32);
        for (Map.Entry<String, Object> entry : all.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String param = ":" + prefix + "_" + key;
            params.put(param, value);
        }
        return params;
    }

}
