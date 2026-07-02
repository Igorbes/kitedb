package pro.kitedb.test.om;

import pro.kitedb.graph.Graphable;
import pro.kitedb.KiteJdbcTemplate;
import pro.kitedb.exception.DataException;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.DataGraphBuilder;
import pro.kitedb.graph.JoinTemplateImpl;
import pro.kitedb.graph.reader.PlainObjectGraph;

public class SaleGraph implements Graphable<Sale, SaleFilter> {
    private final KiteJdbcTemplate jdbcTemplate;

    public SaleGraph(KiteJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DataGraph<Sale, SaleFilter> graph(SaleFilter filter) throws DataException {
        return DataGraphBuilder.<Sale, SaleFilter>graph(Sale.class, filter)
                .key("id")
                .required("userId")
                .required("timestamp")
                .required("amount")
                .required("summary")
                .required(new PlainObjectGraph(Sale.Address.class, "address"))
                .joinTemplate(new JoinTemplateImpl("sql/sale/select.sql.vm", jdbcTemplate))
                .build();
    }

}
