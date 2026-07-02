package pro.kitedb;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public interface KiteJdbcTemplate extends
        InsertJdbcTemplate,
        SelectJdbcTemplate,
        UpdateJdbcTemplate,
        DeleteJdbcTemplate,
        CountJdbcTemplate,
        CopyJdbcTemplate,
        ExecuteJdbcTemplate {
    void init();
    NamedParameterJdbcTemplate getJdbcTemplate();
}
