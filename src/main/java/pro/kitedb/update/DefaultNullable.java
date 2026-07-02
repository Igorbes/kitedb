package pro.kitedb.update;

import java.util.Map;
import java.util.Set;

public class DefaultNullable<T> implements NullableUpdater<T> {
    private final T model;
    private final Set<String> fieldsForUpdate;

    @Deprecated
    public DefaultNullable(T model, Map<String, Object> map) {
        this(model, map.keySet());
    }

    public DefaultNullable(T model, Set<String> fieldsForUpdate) {
        this.model = model;
        this.fieldsForUpdate = fieldsForUpdate;
    }

    @Override
    public T getModel() {
        return this.model;
    }

    @Override
    public DefineCondition getDefineCondition() {
        return new DefineCondition(fieldsForUpdate);
    }
}
