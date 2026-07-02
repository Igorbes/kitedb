package pro.kitedb.test.om;

import pro.kitedb.graph.Graphable;
import pro.kitedb.KiteJdbcTemplate;
import pro.kitedb.exception.DataException;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.graph.DataGraphBuilder;

public class UserGraph implements Graphable<User, UserFilter> {
    private final KiteJdbcTemplate jdbcTemplate;

    public UserGraph(KiteJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DataGraph<User, UserFilter> graph(UserFilter filter) throws DataException {
        return DataGraphBuilder.<User, UserFilter>graph(User.class, filter)
                .key("id")
                .required("name")
                .required("surname")
                .required("nickname")
                .required("sales", (userGraph) -> new SaleGraph(this.jdbcTemplate).graph(userGraph.getFilter().getSaleFilter()))
                .build();
    }
}
