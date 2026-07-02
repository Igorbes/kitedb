package pro.kitedb.ppss;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public interface KiteParameterizedPreparedStatementSetter<T> {
    void setValues(PreparedStatement ps, AtomicInteger index, T model) throws SQLException;
}
