package pro.kitedb.update;


import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface MapSerialisable {
    Map<String, Object> getRawMapRequest();
}
