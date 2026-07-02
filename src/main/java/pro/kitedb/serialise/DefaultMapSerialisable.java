package pro.kitedb.serialise;

import pro.kitedb.update.MapSerialisable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public abstract class DefaultMapSerialisable implements MapSerialisable {
    private Map<String, Object> properties = new HashMap<>();

    @Override
    @JsonIgnore
    public Map<String, Object> getRawMapRequest() {
        return properties;
    }
}
