package pro.kitedb.factory.returning;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturningByColumnIndexNumber implements Returning<Number> {
    private final int columnIndex;

    public ReturningByColumnIndexNumber(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    public Class<Number> getReturnClass() {
        return Number.class;
    }

    @Override
    public Number returning(ResultSet resultSet) throws SQLException {
        return (Number) resultSet.getObject(this.columnIndex);
    }
}
