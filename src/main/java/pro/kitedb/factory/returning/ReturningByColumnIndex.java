package pro.kitedb.factory.returning;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturningByColumnIndex<R> implements Returning<R> {
    private final int columnIndex;
    private final Class<R> returnType;

    public ReturningByColumnIndex(int columnIndex, Class<R> returnType) {
        this.columnIndex = columnIndex;
        this.returnType = returnType;
    }

    @Override
    public Class<R> getReturnClass() {
        return returnType;
    }

    @Override
    public R returning(ResultSet resultSet) throws SQLException {
        return resultSet.getObject(this.columnIndex, this.returnType);
    }
}
