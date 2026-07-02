package pro.kitedb.ppss;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class NopeKitePPSS implements KiteParameterizedPreparedStatementSetter<Object> {
    @Override
    public void setValues(PreparedStatement ps, AtomicInteger index, Object model) throws SQLException {

    }
}
