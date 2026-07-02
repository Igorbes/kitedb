package pro.kitedb.dao;


import org.apache.commons.lang.ArrayUtils;
import pro.kitedb.exception.DataException;
import org.springframework.transaction.annotation.Transactional;

public interface DaoInsert<O, R> extends KiteDao {
    R[] insert(O... models) throws DataException;

    default R insert(O model) throws DataException {
        R[] ids = insert((O[])new Object[]{model});
        return ArrayUtils.isEmpty(ids) ? null : ids[0];
    }
}
