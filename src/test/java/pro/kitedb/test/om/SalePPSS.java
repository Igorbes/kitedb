package pro.kitedb.test.om;

import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class SalePPSS implements KiteParameterizedPreparedStatementSetter<Sale> {

    @Override
    public void setValues(PreparedStatement ps, AtomicInteger index, Sale sale) throws SQLException {
        ps.setInt(index.incrementAndGet(), sale.getUserId());
        ps.setInt(index.incrementAndGet(), sale.getAmount());
        ps.setInt(index.incrementAndGet(), sale.getSummary());
        ps.setInt(index.incrementAndGet(), sale.getAddress().getPostal());
        ps.setString(index.incrementAndGet(), sale.getAddress().getStreet().getName());
        ps.setString(index.incrementAndGet(), sale.getAddress().getCity());
    }

}
