package pro.kitedb.graph;


import pro.kitedb.exception.DataException;
import pro.kitedb.exception.GraphException;
import pro.kitedb.graph.discriminator.Discrimination;
import pro.kitedb.graph.prefix.SimpleClassNameSqlFieldPrefixFactory;
import pro.kitedb.graph.prefix.SimplePredefineClassNameSqlFieldPrefixFactory;
import pro.kitedb.graph.prefix.SqlFieldPrefixFactory;
import pro.kitedb.graph.reader.PlainObjectGraph;
import pro.kitedb.graph.reader.ClassGraphReader;
import pro.kitedb.graph.type.GraphNode;
import pro.kitedb.graph.type.SimpleAliasGraphNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.collections.CollectionUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataGraphBuilder<O, F extends Filter<? super O>> {
    private Map<String, GraphNode<O, F>> graphNodeMap = new HashMap<>();
    private SqlFieldPrefixFactory<O> sqlFieldPrefixFactory;
    private DataGraphFun<DataGraph<O, F>, String> onJoinTemplate;
    private F filter;
    private String sqlTablePrefix;

    public static <O, F extends Filter<? super O>> DataGraphBuilder<O, F> graph(DataGraph<O, F> dataGraph) {
        return dataGraph.toBuilder();
    }

    public static <O, F extends Filter<? super O>> DataGraphBuilder<O, F> graph(SqlFieldPrefixFactory<O> prefixFactory) {
        return new DataGraphBuilder<O, F>(prefixFactory, null);
    }

    public static <O, F extends Filter<? super O>> DataGraphBuilder<O, F> graph(SqlFieldPrefixFactory<O> prefixFactory,F filter) {
        return new DataGraphBuilder<O, F>(prefixFactory, filter);
    }

    public static <O, F extends Filter<? super O>> DataGraphBuilder<O, F> graph(Class<O> clazz) {
        return new DataGraphBuilder<O, F>(new SimpleClassNameSqlFieldPrefixFactory<O>(clazz), null);
    }

    public static <O, F extends Filter<? super O>> DataGraphBuilder<O, F> graph(Class<O> clazz, F filter) {
        return new DataGraphBuilder<O, F>(new SimpleClassNameSqlFieldPrefixFactory<O>(clazz), filter);
    }

    public static <O, F extends Filter<? super O>> DataGraphBuilder<O, F> graph(Class<O> clazz, F filter, DataGraph<? super O, ? super F> parent) {
        DataGraphBuilder<? super O, ? super F> dataGraphBuilder = parent.toBuilder();
        dataGraphBuilder.filter(filter);
        dataGraphBuilder.sqlFieldPrefixFactory = new SimpleClassNameSqlFieldPrefixFactory(clazz);
        return (DataGraphBuilder<O, F>) dataGraphBuilder;
    }

    public static <O, F extends Filter<? super O>> DataGraphBuilder<O, F> graph(String prefix, Class<O> clazz, F filter) {
        return new DataGraphBuilder<O, F>(new SimplePredefineClassNameSqlFieldPrefixFactory<O>(prefix, clazz), filter);
    }

    private DataGraphBuilder(SqlFieldPrefixFactory<O> prefixFactory, F filter) {
        this.sqlFieldPrefixFactory = prefixFactory;
        this.filter = filter;
    }

    public DataGraphBuilder<O, F> required(ClassGraphReader graphReader) throws DataException {
        List<String> graph = graphReader.getGraph();
        if(CollectionUtils.isNotEmpty(graph)) {
            for(String fieldName: graph) {
                putNode(fieldName, new SimpleAliasGraphNode<>(fieldName, sqlFieldPrefixFactory, false));
            }
        }
        return this;
    }

    public DataGraphBuilder<O, F> required(ClassGraphReader graphReader, String key) throws DataException {
        List<String> graph = graphReader.getGraph();
        if(CollectionUtils.isNotEmpty(graph)) {
            for(String fieldName: graph) {
                putNode(fieldName, new SimpleAliasGraphNode<>(fieldName, sqlFieldPrefixFactory, key.equals(fieldName)));
            }
        }
        return this;
    }

    public DataGraphBuilder<O, F> required(String fieldName) throws DataException {
        putNode(fieldName, new SimpleAliasGraphNode<>(fieldName, sqlFieldPrefixFactory, false));
        return this;
    }

    public DataGraphBuilder<O, F> required(String fieldName, Discrimination<O, F> discrimination) throws DataException {
        putNode(fieldName, new SimpleAliasGraphNode<>(fieldName, sqlFieldPrefixFactory, false, discrimination));
        return this;
    }

    public DataGraphBuilder<O, F> key(String fieldName) throws DataException {
        putNode(fieldName, new SimpleAliasGraphNode<>(fieldName, sqlFieldPrefixFactory, true));
        return this;
    }

    public DataGraphBuilder<O, ? super F> filter(F filter) {
        this.filter = filter;
        return this;
    }

    public DataGraphBuilder<O, F> required(String fieldName, DataGraph<?, ?> dataGraph) throws DataException {
        putNode(fieldName, new DataJoinLayerGraphNode(fieldName, (parentGraph) -> dataGraph));
        return this;
    }


    public DataGraphBuilder<O, F> required(String fieldName, DataGraphFun<DataGraph<O, F>, DataGraph<?, ?>> function) throws DataException {
        putNode(fieldName, new DataJoinLayerGraphNode(fieldName, function));
        return this;
    }

    public DataGraphBuilder<O, F> delete(String fieldName) {
        Iterator<Map.Entry<String, GraphNode<O, F>>> iterator = graphNodeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, GraphNode<O, F>> next = iterator.next();
            if(fieldName.equals(next.getKey()) || next.getKey().startsWith(fieldName + ".")) {
                iterator.remove();
            }
        }
        return this;
    }

    public DataGraphBuilder<O, F> delete(String... fieldNames) {
        for (String fieldName : fieldNames) {
            graphNodeMap.remove(fieldName);
        }
        return this;
    }

    public DataGraphBuilder<O, F> intercept(String... requiredFields) {
        return intercept(Arrays.asList(requiredFields));
    }

    public InterceptBuilder<O, F> intercept() {
        return new InterceptBuilder<>(this);
    }

    public DataGraphBuilder<O, F> intercept(PlainObjectGraph... plainObjectGraphs) {
        Collection<String> requiredFields = new ArrayList<>(64);
        for (PlainObjectGraph plainObjectGraph : plainObjectGraphs) {
            List<String> graph = plainObjectGraph.getGraph();
            if(!CollectionUtils.isEmpty(graph)) {
                requiredFields.addAll(graph);
            }
        }
        return intercept(requiredFields.toArray(new String[0]));
    }

    public DataGraphBuilder<O, F> intercept(Collection<String> requiredFields) {
        InterceptBuilder<O, F> builder = new InterceptBuilder<>(this);
        builder.add(requiredFields);
        return builder.build();
    }

    public DataGraph<O, F> build() {
        return new DataGraphImp<>(sqlFieldPrefixFactory, this.sqlTablePrefix, graphNodeMap, onJoinTemplate, this.filter);
    }

    public DataGraphBuilder<O, F> joinTemplate(DataGraphFun<DataGraph<O, F>, String> onJoinTemplate) {
        this.onJoinTemplate = onJoinTemplate;
        return this;
    }

    public DataGraphBuilder<O, F> joinTemplate(JoinTemplate templateName) {
        this.onJoinTemplate = new DataGraphFun<DataGraph<O, F>, String>() {
            @Override
            public String apply(DataGraph<O, F> joinGraph) throws DataException {
                try {
                    return templateName.getJoinTemplate(joinGraph);
                } catch (SQLException e) {
                    throw new DataException(e);
                }
            }
        };
        return this;
    }

    private void putNode(String key, GraphNode<O, F> value) {
        if(graphNodeMap.containsKey(key)) throw new GraphException(String.format("Key \"%s\" already exists", key));

        graphNodeMap.put(key, value);
    }

    public DataGraphBuilder<O, F> prefix(String prefix) {
        return prefix(prefix, sqlFieldPrefixFactory.getClazz());
    }

    public DataGraphBuilder<O, F> prefix(Class<O> oClass) {
        return prefix(sqlFieldPrefixFactory.getPrefix(), oClass);
    }

    public DataGraphBuilder<O, F> prefix(String prefix, Class<? super O> oClass) {
        this.sqlFieldPrefixFactory.setPrefix(prefix);
        this.sqlFieldPrefixFactory.setClazz(oClass);
        return this;
    }

    public DataGraphBuilder<O, F> tablePrefix(String prefix) {
        this.sqlTablePrefix = prefix;
        return this;
    }

    static <O, F extends Filter<? super O>> DataGraphBuilder<O, F> from(DataGraphImp<O, F> dataGraph) {
        DataGraphBuilder<O, F> dataGraphBuilder = new DataGraphBuilder<O, F>(dataGraph.sqlFieldPrefixFactory, dataGraph.getFilter());
        dataGraphBuilder.onJoinTemplate = dataGraph.onJoinTable;
        dataGraphBuilder.graphNodeMap.putAll(dataGraph.graphNodeMap);
        return dataGraphBuilder;
    }

    @Builder
    @AllArgsConstructor
    public static class InterceptBuilder<O, F extends Filter<? super O>> {
        private final DataGraphBuilder<O, F> that;
        private Collection<String> requiredFields = new ArrayList<>(64);

        public InterceptBuilder(DataGraphBuilder<O, F> that) {
            this.that = that;
        }

        public InterceptBuilder<O, F> add(Collection<String> requiredFields) {
            this.requiredFields.addAll(requiredFields);
            return this;
        }

        public DataGraphBuilder<O, F> build() {
            Set<String> keys = this.that.graphNodeMap.keySet();
            if(CollectionUtils.isNotEmpty(keys)) {
                Function<String, Boolean> includesFieldFun = new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String field) {
                        for(String requiredField: requiredFields) {
                            if(field.startsWith(requiredField)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
                Set<String> toRemove = keys.stream().filter(v -> !includesFieldFun.apply(v)).collect(Collectors.toSet());
                for (String key : toRemove) {
                    GraphNode graphNode = this.that.graphNodeMap.get(key);
                    if(graphNode instanceof  SimpleAliasGraphNode) {
                        SimpleAliasGraphNode simpleAliasGraphNode = (SimpleAliasGraphNode) graphNode;
                        if(simpleAliasGraphNode.isKey()) continue;
                    }
                    this.that.graphNodeMap.remove(key);
                }
            }
            return that;
        }
    }
}
