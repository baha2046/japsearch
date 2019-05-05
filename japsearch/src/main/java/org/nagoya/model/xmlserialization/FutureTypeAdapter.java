package org.nagoya.model.xmlserialization;

import com.google.gson.*;
import io.vavr.concurrent.Future;

import java.lang.reflect.Type;

public class FutureTypeAdapter<T> implements JsonDeserializer<Future<T>>, JsonSerializer<Future<T>> {

    private final Type valueType;

    public FutureTypeAdapter(Type type) {
        this.valueType = type;

    }

    @Override
    public Future<T> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        return Future.successful(jsonDeserializationContext.deserialize(jsonElement, this.valueType));
    }

    @Override
    public JsonElement serialize(Future<T> future, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(future.get());
    }
}