package pro.kitedb.factory.copy;

import java.io.OutputStream;
import java.sql.Connection;

public interface CopyManager {
    void copyOut(Connection connection, String sql) throws Exception;
}
