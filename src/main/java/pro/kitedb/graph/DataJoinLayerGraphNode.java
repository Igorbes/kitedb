package pro.kitedb.graph;

import pro.kitedb.exception.DataException;
import pro.kitedb.graph.type.GraphNode;
import lombok.Getter;
import org.simpleflatmapper.jdbc.JdbcMapperFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataJoinLayerGraphNode<O, F extends Filter<? super O>> implements GraphNode<O, F> {
    private final @Getter DataGraphFun<DataGraph<?, ?>, DataGraph<O, F>> function;
    private final @Getter String fieldName;

    public DataJoinLayerGraphNode(String fieldName, DataGraphFun<DataGraph<?, ?>, DataGraph<O, F>> function) {
        this.fieldName = fieldName;
        this.function = new DataGraphFun<DataGraph<?, ?>, DataGraph<O, F>>() {
            @Override
            public DataGraph<O, F> apply(DataGraph<?, ?> parentGraph) throws DataException {
                String prefix = parentGraph.prefix() != null ? parentGraph.prefix() + "." + fieldName : fieldName;
                DataGraph<O, F> graph = function.apply(parentGraph);
                return graph.toBuilder().prefix(prefix).build();
            }
        };
    }

    @Override
    public Map<? extends String, ? extends String> getAlias(DataGraph<O, F> parentGraph, boolean withPrefix) throws DataException {
        Map<String, String> res = new HashMap<>();
        Map<? extends String, ? extends String> alias = function.apply(parentGraph).aliases(withPrefix);
        for(String key: alias.keySet()) {
            res.put(key, fieldName + '.' + alias.get(key));
        }
        return res;
    }

    @Override
    public Map<? extends String, ? extends String> getAlias(DataGraph<O, F> parentGraph) throws DataException {
        Map<String, String> res = new HashMap<>();
        Map<? extends String, ? extends String> alias = function.apply(parentGraph).aliases();
        for(String key: alias.keySet()) {
            res.put(key, fieldName + '.' + alias.get(key));
        }
        return getAlias(parentGraph, true);
    }

    @Override
    public String getAliasName(DataGraph<O, F> parentGraph, String field, boolean withPrefix) throws DataException {
        Set<Map.Entry<String, String>> entries = function.apply(parentGraph).aliases(withPrefix).entrySet();
        for(Map.Entry<String, String> entry: entries) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(value.equals(field)) {
                return key;
            }
        }
        return null;
    }

    @Override
    public String getAliasName(DataGraph<O, F> parentGraph, String field) throws DataException {
        return getAliasName(parentGraph, field, true);
    }

    @Override
    public String[] keys(DataGraph<O, F> parentGraph) throws DataException {
        return keys(parentGraph, true);
    }

    @Override
    public String[] keys(DataGraph<O, F> parentGraph, boolean withPrefix) {
        try {
            return function.apply(parentGraph).keys(withPrefix);
        } catch (DataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void discriminate(DataGraph<O, F> parentGraph, JdbcMapperFactory jdbcMapperFactory) throws DataException {
        DataGraph<?, F> subGraph = function.apply(parentGraph);
        if(subGraph != null) {
            subGraph.discriminator(jdbcMapperFactory);
        }
    }

    @Override
    public boolean isKey() {
        return false;
    }
}
