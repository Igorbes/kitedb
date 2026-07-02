package pro.kitedb.dao;


import org.apache.commons.lang.ArrayUtils;
import pro.kitedb.exception.DataException;
import org.springframework.transaction.annotation.Transactional;

public interface DaoUpdate<MODEL, ID> extends KiteDao {
    ID[] update(MODEL... models) throws DataException;

    default ID update(MODEL model) throws DataException {
        ID[] ids = update((MODEL[])new Object[]{model});
        return ArrayUtils.isEmpty(ids) ? null : ids[0];
    }
}
