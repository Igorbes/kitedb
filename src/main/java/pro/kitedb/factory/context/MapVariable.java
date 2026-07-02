package pro.kitedb.factory.context;

import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
public class MapVariable implements Variable {
    private final HashMap<String, Object> map;

    public MapVariable(HashMap<String, Object> map) {
        this.map = map;
    }

    @Override
    public Map<String, Object> getContext() {
        return map;
    }
}
