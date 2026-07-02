package pro.kitedb.update;

import java.util.Set;

public class DefineCondition {
    private final Set<String> fieldsForUpdate;

    public DefineCondition(MapSerialisable mapSerialisable) {
        this(mapSerialisable.getRawMapRequest().keySet());
    }

    public DefineCondition(Set<String> fieldsForUpdate) {
        this.fieldsForUpdate = fieldsForUpdate;
    }

    public boolean defined(String name) {
        return fieldsForUpdate.contains(name);
    }
}
