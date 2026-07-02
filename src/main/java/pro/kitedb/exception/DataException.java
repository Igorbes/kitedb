package pro.kitedb.exception;

import java.sql.SQLException;

public class DataException extends SQLException {

    public DataException() {
    }

    public DataException(String msg) {
        super(msg);
    }

    public DataException(Throwable e) {
        super(e);
    }
}
