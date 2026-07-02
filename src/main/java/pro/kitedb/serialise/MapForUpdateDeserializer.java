package pro.kitedb.serialise;

import pro.kitedb.update.MapSerialisable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Optional;

public class MapForUpdateDeserializer extends JsonDeserializer<MapSerialisable> implements ContextualDeserializer {
    private JavaType type;

    public MapForUpdateDeserializer() {
    }

    public MapForUpdateDeserializer(JavaType type) {
        this.type = type;
    }

    @Override
    public MapSerialisable deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            HashMap hashMap = deserializationContext.readValue(jsonParser, HashMap.class);
            Class<MapSerialisable> rawClass = (Class<MapSerialisable>) type.getRawClass();
            MapSerialisable result = rawClass.newInstance();
            doFillPresentProperties(result, hashMap, (ObjectMapper) jsonParser.getCodec());
            return result;
        } catch (Exception r) {
            throw new RuntimeException(r);
        }
    }

    protected void doFillPresentProperties(MapSerialisable requestMapSerialised, HashMap hashMap, ObjectMapper codec) {
        try {
            Field[] declaredFields = requestMapSerialised.getClass().getDeclaredFields();
            for(Field field: declaredFields) {
                String name = field.getName();
                Type genericType = field.getGenericType();

                if(hashMap.containsKey(name)) {
                    Object val = hashMap.get(name);
                    requestMapSerialised.getRawMapRequest().put(name, val);
                    field.setAccessible(true);
                    field.set(requestMapSerialised, codec.readValue(codec.writeValueAsBytes(val), new TypeReference<Object>() {
                        @Override
                        public Type getType() {
                            return genericType;
                        }
                    }));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        JavaType type = Optional.ofNullable(deserializationContext.getContextualType()).orElse(Optional.ofNullable(beanProperty).map(BeanProperty::getMember).map(Annotated::getType).orElse(null));
        if(type == null) throw new IllegalArgumentException("Java type not found");
        return new MapForUpdateDeserializer(type);
    }
}
