package pro.kitedb.factory.context;

import lombok.ToString;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Map;

@ToString
public class MapSqlParameter implements Variable {
    private final MapSqlParameterSource mapSqlParameterSource;

    public MapSqlParameter(MapSqlParameterSource mapSqlParameterSource) {
        this.mapSqlParameterSource = mapSqlParameterSource;
    }

    @Override
    public Map<String, Object> getContext() {
        return mapSqlParameterSource.getValues();
    }
}
