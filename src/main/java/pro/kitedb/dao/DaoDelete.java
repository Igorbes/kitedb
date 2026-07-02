package pro.kitedb.dao;


import org.apache.commons.lang.ArrayUtils;
import pro.kitedb.exception.DataException;
import org.springframework.transaction.annotation.Transactional;

public interface DaoDelete<I> extends KiteDao {
    I[] delete(I... models) throws DataException;

    default I delete(I model) throws DataException {
        I[] ids = delete((I[])new Object[]{model});
        return ArrayUtils.isEmpty(ids) ? null : ids[0];
    }
}
