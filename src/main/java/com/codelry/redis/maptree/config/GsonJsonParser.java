package com.codelry.redis.maptree.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.lettuce.core.json.JsonArray;
import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import io.lettuce.core.json.JsonValue;
import jakarta.validation.constraints.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public class GsonJsonParser implements JsonParser {
  private final Gson gson;

  public GsonJsonParser() {
    this.gson = new Gson();
  }

  public GsonJsonParser(Gson gson) {
    this.gson = gson;
  }

  @Override
  public JsonValue loadJsonValue(ByteBuffer byteBuffer) {
    String json = StandardCharsets.UTF_8.decode(byteBuffer).toString();
    return createJsonValue(json);
  }

  @Override
  public JsonValue createJsonValue(ByteBuffer byteBuffer) {
    String json = StandardCharsets.UTF_8.decode(byteBuffer).toString();
    return createJsonValue(json);
  }

  @Override
  public JsonValue createJsonValue(String s) {
    JsonElement element = gson.fromJson(s, JsonElement.class);
    return new GsonJsonValue(element, gson);
  }

  @Override
  public JsonObject createJsonObject() {
    return new GsonJsonValue(new com.google.gson.JsonObject(), gson).asJsonObject();
  }

  @Override
  public JsonArray createJsonArray() {
    return new GsonJsonValue(new com.google.gson.JsonArray(), gson).asJsonArray();
  }

  @Override
  public JsonValue fromObject(Object o) {
    String json = gson.toJson(o);
    return createJsonValue(json);
  }

  private record GsonJsonValue(JsonElement element, Gson gson) implements JsonValue, JsonObject, JsonArray {
    @Override
    public boolean isJsonArray() {
      return element.isJsonArray();
    }

    @Override
    public JsonArray asJsonArray() {
      return isJsonArray() ? this : null;
    }

    @Override
    public boolean isJsonObject() {
      return element.isJsonObject();
    }

    @Override
    public JsonObject asJsonObject() {
      return isJsonObject() ? this : null;
    }

    @Override
    public boolean isString() {
      return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }

    @Override
    public boolean isNumber() {
      return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }

    @Override
    public Number asNumber() {
      if (isNumber()) {
        return element.getAsNumber();
      }
      return null;
    }

    @Override
    public boolean isBoolean() {
      return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
    }

    @Override
    public Boolean asBoolean() {
      if (isBoolean()) {
        return element.getAsBoolean();
      }
      return null;
    }

    @Override
    public boolean isNull() {
      return element.isJsonNull();
    }

    @Override
    public <T> T toObject(Class<T> aClass) {
      return gson.fromJson(element, aClass);
    }

    @Override
    public String asString() {
      return element.getAsString();
    }

    @Override
    @NotNull
    public String toString() {
      return gson.toJson(element);
    }

    @Override
    public ByteBuffer asByteBuffer() {
      String json = gson.toJson(element);
      return StandardCharsets.UTF_8.encode(json);
    }

    @Override
    public JsonArray add(JsonValue jsonValue) {
      if (!isJsonArray()) {
        throw new UnsupportedOperationException("Not a JSON array");
      }
      com.google.gson.JsonArray array = element.getAsJsonArray();
      if (jsonValue instanceof GsonJsonValue gsonValue) {
        array.add(gsonValue.element);
      }
      return this;
    }

    @Override
    public void addAll(JsonArray jsonArray) {
      if (!isJsonArray()) {
        throw new UnsupportedOperationException("Not a JSON array");
      }
      com.google.gson.JsonArray array = element.getAsJsonArray();
      if (jsonArray instanceof GsonJsonValue gsonValue && gsonValue.isJsonArray()) {
        gsonValue.element.getAsJsonArray().forEach(array::add);
      }
    }

    @Override
    public List<JsonValue> asList() {
      if (!isJsonArray()) {
        return List.of();
      }
      com.google.gson.JsonArray array = element.getAsJsonArray();
      return java.util.stream.StreamSupport.stream(array.spliterator(), false)
          .map(e -> new GsonJsonValue(e, gson))
          .map(v -> (JsonValue) v)
          .toList();
    }

    @Override
    public JsonValue get(int i) {
      if (!isJsonArray()) {
        return null;
      }
      com.google.gson.JsonArray array = element.getAsJsonArray();
      if (i >= 0 && i < array.size()) {
        return new GsonJsonValue(array.get(i), gson);
      }
      return null;
    }

    @Override
    public JsonValue getFirst() {
      if (!isJsonArray()) {
        return null;
      }
      com.google.gson.JsonArray array = element.getAsJsonArray();
      if (!array.isEmpty()) {
        return new GsonJsonValue(array.get(0), gson);
      }
      return null;
    }

    @Override
    public Iterator<JsonValue> iterator() {
      if (!isJsonArray()) {
        return java.util.Collections.emptyIterator();
      }
      com.google.gson.JsonArray array = element.getAsJsonArray();
      return java.util.stream.StreamSupport.stream(array.spliterator(), false)
          .map(e -> (JsonValue) new GsonJsonValue(e, gson))
          .iterator();
    }

    @Override
    public JsonValue remove(int i) {
      if (!isJsonArray()) {
        return null;
      }
      com.google.gson.JsonArray array = element.getAsJsonArray();
      if (i >= 0 && i < array.size()) {
        JsonElement removed = array.remove(i);
        return new GsonJsonValue(removed, gson);
      }
      return null;
    }

    @Override
    public JsonValue replace(int i, JsonValue jsonValue) {
      if (!isJsonArray()) {
        return null;
      }
      com.google.gson.JsonArray array = element.getAsJsonArray();
      if (i >= 0 && i < array.size() && jsonValue instanceof GsonJsonValue gsonValue) {
        JsonElement old = array.set(i, gsonValue.element);
        return new GsonJsonValue(old, gson);
      }
      return null;
    }

    @Override
    public JsonObject put(String key, JsonValue jsonValue) {
      if (!isJsonObject()) {
        throw new UnsupportedOperationException("Not a JSON object");
      }
      com.google.gson.JsonObject obj = element.getAsJsonObject();
      if (jsonValue instanceof GsonJsonValue gsonValue) {
        obj.add(key, gsonValue.element);
      }
      return this;
    }

    @Override
    public JsonValue get(String key) {
      if (!isJsonObject()) {
        return null;
      }
      com.google.gson.JsonObject obj = element.getAsJsonObject();
      JsonElement value = obj.get(key);
      return value != null ? new GsonJsonValue(value, gson) : null;
    }

    @Override
    public JsonValue remove(String key) {
      if (!isJsonObject()) {
        return null;
      }
      com.google.gson.JsonObject obj = element.getAsJsonObject();
      JsonElement removed = obj.remove(key);
      return removed != null ? new GsonJsonValue(removed, gson) : null;
    }

    @Override
    public int size() {
      if (isJsonArray()) {
        return element.getAsJsonArray().size();
      } else if (isJsonObject()) {
        return element.getAsJsonObject().size();
      }
      return 0;
    }
  }
}
