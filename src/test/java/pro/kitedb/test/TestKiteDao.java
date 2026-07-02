package pro.kitedb.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pro.kitedb.KiteJdbcTemplate;
import pro.kitedb.KiteJdbcTemplateImpl;
import pro.kitedb.dao.DaoFacade;
import pro.kitedb.dao.DaoFullAccess;
import pro.kitedb.dao.annotation.*;
import pro.kitedb.exception.DataException;
import pro.kitedb.factory.KiteDB;
import pro.kitedb.test.cm.PostgresCopyManager;
import pro.kitedb.test.om.*;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assume.assumeTrue;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestKiteDao {
    private static KiteJdbcTemplateImpl jdbcTemplate;
    private static PostgreSQLContainer postgreSQLContainer;
    private static HikariDataSource hikariDataSource;
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
        hikariDataSource = new HikariDataSource(config);

        jdbcTemplate = new KiteJdbcTemplateImpl(hikariDataSource);
        jdbcTemplate.init();

        jdbcTemplate.execute("init.sql.vm");
    }

    @Test()
    public void test01_userdao() throws Exception {
        assumeTrue(counter == 1);
        UserDao userDao = new KiteDB(hikariDataSource).create(UserDao.class);
        User user = new User("Ivan", "Petrov");
        Number id = userDao.insert(user);
        user.setId(id.intValue());

        List<User> users = Arrays.asList(
                new User("Alexander", "Ivanov"),
                new User("Dmitry", "Sokolov"),
                new User("Sergey", "Kuznetsov")
        );
        Number[] ids = userDao.insert(users.toArray(new User[0]));
        for (int i = 0; i < ids.length; i++) {
            users.get(i).setId(ids[i].intValue());
        }

        User[] select = userDao.select(new UserGraph(jdbcTemplate).graph(UserFilter.builder().build()));
        Assert.assertEquals(4, select.length);

        String nickname = "My nick";
        user.setNickname(nickname);
        Number updated = userDao.update(user);

        User compared = userDao.selectForObject(new UserGraph(jdbcTemplate)
                .graph(UserFilter.builder()
                        .id(updated.intValue())
                        .build()));
        Assert.assertEquals(updated, compared.getId());
        Assert.assertNotNull(compared.getName());
        Assert.assertNotNull(compared.getSurname());
        Assert.assertEquals(nickname, compared.getNickname());

        userDao.delete(compared.getId());
        long count = userDao.count(new UserGraph(jdbcTemplate).graph(UserFilter.builder().build()));
        Assert.assertEquals(3L, count);
        counter++;
    }

    @Test()
    public void test02_copydao() throws Exception {
        assumeTrue(counter == 2);
        UserDao userDao = new KiteDB(hikariDataSource).create(UserDao.class);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PostgresCopyManager cp = new PostgresCopyManager(baos);
        userDao.copy(new UserGraph(jdbcTemplate).graph(UserFilter.builder().build()), cp);

        Assert.assertEquals(true, baos.toByteArray().length > 0);
        counter++;
    }

    @Test()
    public void test03_extend() throws Exception {
        assumeTrue(counter == 3);
        UserExtendDao userDao = new UserExtendDao(jdbcTemplate);
        long count = userDao.myMethod();
        Assert.assertEquals(3L, count);
    }

    @DaoInsertAbility(template = "sql/user/insert.sql.vm", clazz = User.class, ppss = UserInsertPPSS.class, batchSize = 2)
    @DaoSelectAbility(template = "sql/user/select.sql.vm", graphable = UserGraph.class)
    @DaoUpdateAbility(template = "sql/user/upsert.sql.vm", ppss = UserUpsertPPSS.class)
    @DaoDeleteAbility(template = "sql/user/delete.sql.vm")
    @DaoCopyAbility(template = "sql/user/select.sql.vm")
    public interface UserDao extends DaoFullAccess<Number, User, UserFilter> {

    }

    class UserExtendDao extends DaoFacade<Number, User, UserFilter> {
        private final @Getter UserDao implementation;
        private final @Getter KiteJdbcTemplate jdbcTemplate;

        public UserExtendDao(KiteJdbcTemplate kiteJdbcTemplate) {
            this.jdbcTemplate = kiteJdbcTemplate;
            this.implementation = new KiteDB(kiteJdbcTemplate.getJdbcTemplate().getJdbcTemplate().getDataSource()).create(UserDao.class);
        }

        public long myMethod() throws DataException {
            return this.jdbcTemplate.count("sql/user/select.sql.vm", new UserGraph(jdbcTemplate).graph(UserFilter.builder().build()));
        }
    }
}
