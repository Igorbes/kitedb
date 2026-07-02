package pro.kitedb.factory.returning;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturningFirstNumberColumn implements Returning<Number> {
    @Override
    public Class<Number> getReturnClass() {
        return Number.class;
    }

    @Override
    public Number returning(ResultSet resultSet) throws SQLException {
        return (Number) resultSet.getObject(1);
    }
}
