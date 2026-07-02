package pro.kitedb.factory.returning;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Returning<R> {
    Class<R> getReturnClass();
    R returning(ResultSet resultSet) throws SQLException;
}
