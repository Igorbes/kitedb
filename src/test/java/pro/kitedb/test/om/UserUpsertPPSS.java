package pro.kitedb.test.om;

import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class UserUpsertPPSS implements KiteParameterizedPreparedStatementSetter<User> {
    @Override
    public void setValues(PreparedStatement ps, AtomicInteger index, User model) throws SQLException {
        ps.setInt(index.incrementAndGet(), model.getId());
        ps.setString(index.incrementAndGet(), model.getName());
        ps.setString(index.incrementAndGet(), model.getSurname());
        ps.setString(index.incrementAndGet(), model.getNickname());
    }
}
