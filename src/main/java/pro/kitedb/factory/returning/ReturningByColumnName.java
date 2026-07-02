package pro.kitedb.factory.returning;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturningByColumnName<R> implements Returning<R> {
    private final String columnName;
    private final Class<R> returnType;

    public ReturningByColumnName(String columnName, Class<R> returnType) {
        this.columnName = columnName;
        this.returnType = returnType;
    }

    @Override
    public Class<R> getReturnClass() {
        return returnType;
    }

    @Override
    public R returning(ResultSet resultSet) throws SQLException {
        return resultSet.getObject(this.columnName, this.returnType);
    }
}
