package pro.kitedb.graph;

import pro.kitedb.exception.DataException;
import pro.kitedb.graph.type.SimpleAliasGraphNode;
import org.springframework.util.ClassUtils;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class DataGraphUtils {

    public static Map<String, Object> toMap(DataGraphImp<?, ?> dataGraph) throws DataException {
        if (dataGraph.graphNodeMap != null) {
            Map<String, Object> toStringMap = new HashMap<>(dataGraph.graphNodeMap);
            for (String key : toStringMap.keySet()) {
                Object graphNode = toStringMap.get(key);
                if (graphNode instanceof SimpleAliasGraphNode) {
                    SimpleAliasGraphNode simpleAliasGraphNode = (SimpleAliasGraphNode) graphNode;
                    toStringMap.put(key, simpleAliasGraphNode.getAliasName(dataGraph, key));
                } else if (graphNode instanceof DataJoinLayerGraphNode) {
                    DataJoinLayerGraphNode dataJoinLayerGraphNode = (DataJoinLayerGraphNode) graphNode;
                    DataGraphFun<DataGraph<?, ?>, DataGraph<?, ?>> function = dataJoinLayerGraphNode.getFunction();
                    DataGraph<?, ?> graph = function.apply(dataGraph);
                    if (graph instanceof DataGraphImp) {
                        toStringMap.put(key, DataGraphUtils.toMap((DataGraphImp<?, ?>) graph));
                    }
                }
            }
        }
        return new HashMap<>();
    }

    public static boolean isSimpleValueType(Class<?> clazz) {
        return ClassUtils.isPrimitiveOrWrapper(clazz) || Enum.class.isAssignableFrom(clazz) || CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || URI.class == clazz || URL.class == clazz || Locale.class == clazz || Class.class == clazz;
    }

    public static String toArrayString(Collection<?> changes) {
        return changes.stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}
