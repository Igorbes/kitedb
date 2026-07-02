package pro.kitedb.test.cm;

import org.postgresql.core.BaseConnection;
import pro.kitedb.factory.copy.CopyManager;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class PostgresCopyManager implements CopyManager {
    private final OutputStream os;

    public PostgresCopyManager(OutputStream os) throws SQLException {
        this.os = os;
    }

    @Override
    public void copyOut(Connection connection, String sql) throws Exception {
        org.postgresql.copy.CopyManager copyManager = new org.postgresql.copy.CopyManager(connection.unwrap(BaseConnection.class));

        copyManager.copyOut(sql, this.os);
    }
}
