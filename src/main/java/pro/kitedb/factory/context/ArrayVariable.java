package pro.kitedb.factory.context;

import lombok.ToString;
import pro.kitedb.utils.KiteUtils;

import java.util.Map;

@ToString
public class ArrayVariable implements Variable {
    private final Object[] args;

    public ArrayVariable(Object... args) {
        this.args = args;
    }

    @Override
    public Map<String, Object> getContext() {
        return KiteUtils.toMap(args);
    }
}
