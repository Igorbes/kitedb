package pro.kitedb.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pro.kitedb.KiteJdbcTemplateImpl;
import pro.kitedb.factory.context.ArrayVariable;
import pro.kitedb.factory.returning.Returning;
import pro.kitedb.factory.returning.ReturningByColumnIndex;
import pro.kitedb.factory.returning.ReturningByColumnIndexNumber;
import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;
import pro.kitedb.exception.DataException;
import pro.kitedb.graph.DataGraph;
import pro.kitedb.test.cm.PostgresCopyManager;
import pro.kitedb.test.om.*;

import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assume.assumeTrue;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestJdbcTemplate {

    private static KiteJdbcTemplateImpl jdbcTemplate;
    private static PostgreSQLContainer postgreSQLContainer;

    private static int counter = 1;

    @BeforeClass
    public static void init() throws DataException {
        postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
        postgreSQLContainer.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        config.setUsername(postgreSQLContainer.getUsername());
        config.setPassword(postgreSQLContainer.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "1024");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
        config.setPoolName("Hikari postgres pool");
        config.setDriverClassName(postgreSQLContainer.getDriverClassName());
        HikariDataSource hikariDataSource = new HikariDataSource(config);

        jdbcTemplate = new KiteJdbcTemplateImpl(hikariDataSource);
        jdbcTemplate.init();

        jdbcTemplate.execute("init.sql.vm");
    }

    @org.junit.Test
    public void test01_insert() throws DataException {
        assumeTrue(counter == 1);
        List<Integer> summary = new ArrayList<>();
        User user = new User("Ivan", "Petrov");
        KiteParameterizedPreparedStatementSetter<User> ppss = new UserInsertPPSS();
        Number id = jdbcTemplate.insert("sql/user/insert.sql.vm", user, ppss);
        user.setId(id.intValue());
        summary.add(user.getId());

        List<User> users = Arrays.asList(
            new User("Alexander", "Ivanov"),
            new User("Dmitry", "Sokolov"),
            new User("Sergey", "Kuznetsov")
        );
        Number[] ids = jdbcTemplate.insert("sql/user/insert.sql.vm", new ArrayVariable("nickname", null), ppss, users);
        for (int i = 0; i < users.size(); i++) {
            users.get(i).setId(ids[i].intValue());
        }
        summary.addAll(users.stream().map(v -> v.getId()).collect(Collectors.toList()));

        user = new User("Alexey", "Smirnov");
        id = jdbcTemplate.insert("sql/user/insert.sql.vm", new ArrayVariable("nickname", "Superuser"), user, ppss);
        user.setId(id.intValue());
        summary.add(user.getId());

        DataGraph<User, UserFilter> userDataGraph = new UserGraph(jdbcTemplate).graph(UserFilter.builder().withoutNickname(true).build());
        User[] select = jdbcTemplate.select("sql/user/select.sql.vm", userDataGraph);
        Assert.assertEquals(4, select.length);

        long count = jdbcTemplate.count("sql/user/select.sql.vm", userDataGraph);
        Assert.assertEquals(4, count);

        KiteParameterizedPreparedStatementSetter<Sale> ppssSale = new SalePPSS();
        for (int userId : summary) {
            List<Sale> sales = this.generateRandomSale(userId);
            Number[] saleIds = jdbcTemplate.insert("sql/sale/insert.sql.vm", ppssSale, sales);
            for (int i = 0; i < sales.size(); i++) {
                sales.get(i).setId(saleIds[i].intValue());
            }
        }

        for (int userId : summary) {
            List<Sale> sales = this.generateRandomSale(userId);
            Sale[] result = jdbcTemplate.insert("sql/sale/insert.sql.vm", new Returning<Sale>() {
                @Override
                public Class<Sale> getReturnClass() {
                    return Sale.class;
                }

                @Override
                public Sale returning(ResultSet resultSet) throws SQLException {
                    return new Sale(
                            resultSet.getInt("id"),
                            resultSet.getInt("user_id"),
                            resultSet.getTimestamp("timestamp").toLocalDateTime().toLocalDate(),
                            resultSet.getInt("amount"),
                            resultSet.getInt("summary"),
                            new Sale.Address(
                                    resultSet.getInt("postal"),
                                    resultSet.getString("city"),
                                    new Sale.Street(resultSet.getString("street"))
                            )
                    );
                }
            }, ppssSale, sales);
            for (Sale sale : result) {
                Assert.assertNotNull(sale.getId());
                Assert.assertNotNull(sale.getUserId());
                Assert.assertNotNull(sale.getTimestamp());
                Assert.assertNotNull(sale.getAmount());
                Assert.assertNotNull(sale.getSummary());
                Assert.assertNotNull(sale.getAddress());
            }
            List<Integer> saleIds = Arrays.stream(result).map(Sale::getId).collect(Collectors.toList());
            jdbcTemplate.delete("sql/sale/delete.sql.vm", new ArrayVariable("ids", saleIds));
        }
        this.counter++;
    }

    @org.junit.Test
    public void test02_update() throws DataException {
        assumeTrue(counter == 2);
        DataGraph<User, UserFilter> userDataGraph = new UserGraph(jdbcTemplate).graph(UserFilter.builder().withoutNickname(true).build());
        long count = jdbcTemplate.count("sql/user/select.sql.vm", userDataGraph);
        Assert.assertEquals(4, count);

        User[] users = jdbcTemplate.select("sql/user/select.sql.vm", userDataGraph);
        Assert.assertEquals(4, users.length);

        for (User user : users) {
            user.setNickname("No nickname");
        }

        Number update = jdbcTemplate.update("sql/user/update.sql.vm", new ArrayVariable("nickname", "No nick"));
        Assert.assertEquals(4, update.intValue());
        this.counter++;
    }

    @org.junit.Test
    public void test03_select() throws DataException {
        assumeTrue(counter == 3);
        DataGraph<Sale, SaleFilter> saleDataGraph = new SaleGraph(jdbcTemplate).graph(SaleFilter.builder().build());
        DataGraph<User, UserFilter> userDataGraph = new UserGraph(jdbcTemplate).graph(UserFilter.builder().build());

        Sale[] salesCollection = jdbcTemplate.select("sql/sale/select.sql.vm", saleDataGraph);
        Assert.assertEquals(true, ArrayUtils.isNotEmpty(salesCollection));

        User[] users = jdbcTemplate.select("sql/user/select.sql.vm", userDataGraph);
        Assert.assertEquals(5, users.length);

        for (User user : users) {
            Assert.assertNotNull(user.getId());
            Assert.assertNotNull(user.getName());
            Assert.assertNotNull(user.getSurname());
            Assert.assertNotNull(user.getNickname());
            Assert.assertNotNull(user.getSales());

            Collection<Sale> sales = user.getSales();
            for (Sale sale : sales) {
                Assert.assertNotNull(sale.getId());
                Assert.assertNotNull(sale.getUserId());
                Assert.assertNotNull(sale.getTimestamp());
                Assert.assertNotNull(sale.getAmount());
                Assert.assertNotNull(sale.getSummary());
                Assert.assertNotNull(sale.getAddress());
                Assert.assertNotNull(sale.getAddress().getPostal());
                Assert.assertNotNull(sale.getAddress().getCity());
                Assert.assertNotNull(sale.getAddress().getStreet());
                Assert.assertNotNull(sale.getAddress().getStreet().getName());
            }
        }

        users = jdbcTemplate.select("sql/user/select.sql.vm", userDataGraph
                .toBuilder()
                .delete("nickname", "sales")
                .build());
        Assert.assertEquals(5, users.length);

        for (User user : users) {
            Assert.assertNotNull(user.getId());
            Assert.assertNotNull(user.getName());
            Assert.assertNotNull(user.getSurname());
            Assert.assertNull(user.getNickname());
            Assert.assertNull(user.getSales());
        }

        users = jdbcTemplate.select("sql/user/select.sql.vm", userDataGraph
                .toBuilder()
                        .filter(UserFilter.builder()
                                .saleFilter(SaleFilter.builder().amount(100).build())
                                .build())
                .build());
        for (User user : users) {
            Collection<Sale> sales = user.getSales();
            Assert.assertEquals(5, sales.size());
            for (Sale sale : sales) {
                Assert.assertEquals(100, sale.getAmount().intValue());
            }
        }
        this.counter++;
    }

    @org.junit.Test
    public void test04_delete() throws DataException {
        assumeTrue(counter == 4);
        Number[] deletedIds = jdbcTemplate.delete("sql/user/delete.sql.vm", new ArrayVariable("name", "Ivan"));
        Assert.assertEquals(1, deletedIds.length);
        Assert.assertEquals(1, deletedIds[0]);

        DataGraph<User, UserFilter> userDataGraph = new UserGraph(jdbcTemplate).graph(UserFilter.builder().build());
        long count = jdbcTemplate.count("sql/user/select.sql.vm", userDataGraph);
        Assert.assertEquals(4, count);
        this.counter++;
    }

    @org.junit.Test
    public void test05_copy() throws Exception {
        assumeTrue(counter == 5);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jdbcTemplate.copy(
                "sql/user/select.sql.vm",
                new UserGraph(jdbcTemplate).graph(UserFilter.builder().build()),
                new PostgresCopyManager(baos)
        );
        Assert.assertEquals(true, baos.toByteArray().length > 0);
        this.counter++;
    }

    @org.junit.Test
    public void test06_update_v2() throws DataException {
        assumeTrue(counter == 6);
        DataGraph<User, UserFilter> userDataGraph = new UserGraph(jdbcTemplate).graph(UserFilter.builder().build());
        User[] select = jdbcTemplate.select("sql/user/select.sql.vm", userDataGraph);

        jdbcTemplate.update("sql/user/update.v2.sql.vm", new ArrayVariable("nickname", "Kitty"), new ReturningByColumnIndexNumber(1), new KiteParameterizedPreparedStatementSetter<User>() {
            @Override
            public void setValues(PreparedStatement ps, AtomicInteger index, User model) throws SQLException {
                ps.setObject(index.incrementAndGet(), model.getName());
                ps.setObject(index.incrementAndGet(), model.getSurname());
            }
        }, Arrays.asList(select));
        counter++;
    }

    private List<Sale> generateRandomSale(int userId) {
        List<Sale> sales = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            sales.add(new Sale(
                    userId,
                    i % 2 != 0 ? 100 : 200,
                    i % 2 != 0 ? 100 : 200,
                    new Sale.Address(
                            ThreadLocalRandom.current().nextInt(1, 100000),
                            "a",
                            Sale.Street.builder().name("b").build()
                    )
            ));
        }
        return sales;
    }

}
