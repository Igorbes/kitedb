package pro.kitedb.factory.context;

import java.util.Map;

public interface Variable {
    Variable EMPTY = new ArrayVariable();
    Map<String, Object> getContext();
}
