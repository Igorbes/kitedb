package pro.kitedb.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pro.kitedb.KiteJdbcTemplateImpl;
import pro.kitedb.exception.DataException;
import pro.kitedb.factory.context.ArrayVariable;
import pro.kitedb.factory.returning.ReturningByColumnIndex;
import pro.kitedb.graph.*;
import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;
import pro.kitedb.test.om.*;
import pro.kitedb.graph.discriminator.Discriminators;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assume.assumeTrue;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDiscrimiators {

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
    public void test01_discriminator() throws DataException {
        assumeTrue(counter == 1);

        jdbcTemplate.insert("sql/product/insert.coupon.sql.vm", new ReturningByColumnIndex<String>(1, String.class), (KiteParameterizedPreparedStatementSetter<String>) (ps, index, model) -> ps.setString(index.incrementAndGet(), model), Arrays.asList("COUPON_10", "COUPON_20", "COUPON_30"));

        List<Product> list = Arrays.asList(
                Product.builder().name("Juice").build(),
                Product.builder().name("Milk").build(),
                Product.builder().name("Bread").build()
        );
        Integer[] ids = jdbcTemplate.insert("sql/product/insert.sql.vm", new ReturningByColumnIndex<Integer>(1, Integer.class), (KiteParameterizedPreparedStatementSetter<Product>) (ps, index, model) -> ps.setString(index.incrementAndGet(), model.getName()), list);
        for (int i = 0; i < ids.length; i++) {
            for(int j = 0; j < (i + 1); j++) {
                jdbcTemplate.insert("sql/product/insert.discount.sql.vm", new ReturningByColumnIndex<Integer>(1, Integer.class), new ArrayVariable("product_id", ids[i], "discount", j * 10));
            }
        }

        Product[] result = jdbcTemplate.select("sql/product/select.sql.vm", DataGraphBuilder.graph(Product.class)
                .key("id")
                .required("name")
                .required("discount")
                .required("amount")
                .required("coupon", Discriminators.discriminateToArray())
                .build());
        Assert.assertEquals(3, result.length);
        for (Product product : result) {
            Assert.assertEquals(3, product.getCoupons().length);
            switch (product.getName()) {
                case "Juice": {
                    Assert.assertEquals(1, product.getDiscount().size());
                }; break;
                case "Milk": {
                    Assert.assertEquals(2, product.getDiscount().size());
                }; break;
                case "Bread": {
                    Assert.assertEquals(3, product.getDiscount().size());
                }; break;
            }
        }
        this.counter++;
    }

}
