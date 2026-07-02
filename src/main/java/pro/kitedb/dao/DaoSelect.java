package pro.kitedb.dao;


import org.apache.commons.lang3.ArrayUtils;
import org.springframework.transaction.annotation.Transactional;
import pro.kitedb.graph.DataGraphBuilder;
import pro.kitedb.graph.DataGraphImp;
import pro.kitedb.graph.Filter;
import pro.kitedb.exception.DataException;
import pro.kitedb.graph.DataGraph;

public interface DaoSelect<O, F extends Filter<? super O>> extends KiteDao {
    O[] select(DataGraph<O, ? super F> dataGraph) throws DataException;

    default O selectForObject(DataGraph<O, ? super F> dataGraph) throws DataException {
        O[] result = select(dataGraph);
        if(ArrayUtils.isEmpty(result)) return null;

        if(result.length > 1) throw new DataException(String.format("Incorrect result. Expect 1 actual %s", result.length));

        return result[0];
    }
}
