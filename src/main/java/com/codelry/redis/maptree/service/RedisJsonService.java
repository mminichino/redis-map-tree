package com.codelry.redis.maptree.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

@Service
public class RedisJsonService {

  private final RedisConnectionFactory connectionFactory;
  private final Gson gson;

  public RedisJsonService(RedisConnectionFactory connectionFactory,
                          Gson gson) {
    this.connectionFactory = connectionFactory;
    this.gson = gson;
  }

  public String jsonSet(String key, String json) {
    return jsonSet(key, "$", json.getBytes());
  }

  public String jsonSet(String key, byte[] json) {
    return jsonSet(key, "$", json);
  }

  public String jsonSet(String key, Object value) {
    String json = gson.toJson(value);
    return jsonSet(key, "$", json.getBytes());
  }

  public String jsonSet(String key, String path, Object value) {
    String json = gson.toJson(value);
    return jsonSet(key, path, json.getBytes());
  }

  public String jsonSet(String key, String path, byte[] json) {
    try {
      try (RedisConnection connection = connectionFactory.getConnection()) {
        Object result = connection.execute("JSON.SET",
            key.getBytes(),
            path.getBytes(),
            json);
        return result != null ? new String((byte[]) result) : null;
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to set JSON for key: " + key, e);
    }
  }

  public JsonElement jsonGet(String key) {
    return jsonGet(key, "$");
  }

  public JsonElement jsonGet(String key, String path) {
    try {
      String json = getJsonAsString(key, path);
      if (json == null) {
        return null;
      }
      return gson.fromJson(json, JsonElement.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get JSON for key: " + key, e);
    }
  }

  public String getJsonAsString(String key, String path) {
    try (RedisConnection connection = connectionFactory.getConnection()) {
      Object result = connection.execute("JSON.GET",
          key.getBytes(),
          path.getBytes());

      if (result == null) {
        return null;
      }

      return new String((byte[]) result);
    }
  }

  public Long deleteJson(String key, String path) {
    try (RedisConnection connection = connectionFactory.getConnection()) {
      Object result = connection.execute("JSON.DEL",
          key.getBytes(),
          path.getBytes());
      return result != null ? ((Number) result).longValue() : 0L;
    }
  }

  public boolean exists(String key) {
    try (RedisConnection connection = connectionFactory.getConnection()) {
      Object result = connection.execute("JSON.TYPE",
          key.getBytes(),
          "$".getBytes());
      return result != null;
    }
  }
}
