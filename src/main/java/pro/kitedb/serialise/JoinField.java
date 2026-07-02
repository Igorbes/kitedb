package pro.kitedb.serialise;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;

public class JoinField<T> {
    private @Getter @Setter String json;
    private Gson gson = new GsonBuilder().create();

    public T getValue(Class<T> tClass) {
        if(json != null) {
            return gson.fromJson(json, tClass);
        }
        return null;
    }

    public void setValue(T value) {
        if(value != null) {
            json = gson.toJson(value);
        }
    }
}
