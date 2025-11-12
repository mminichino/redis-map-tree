package com.codelry.redis.maptree.config;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import io.lettuce.core.json.JsonPath;
import io.lettuce.core.json.JsonType;
import io.lettuce.core.json.JsonValue;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.List;
import java.util.function.Function;

public class RedisModulesTemplate {

  private final GenericObjectPool<StatefulRedisModulesConnection<String, String>> pool;

  public RedisModulesTemplate(GenericObjectPool<StatefulRedisModulesConnection<String, String>> pool) {
    this.pool = pool;
  }

  public void jsonSet(String key, JsonPath path, String json) {
    executeVoid(commands -> commands.jsonSet(key, path, json));
  }

  public List<JsonValue> jsonGet(String key) {
    return execute(commands -> commands.jsonGet(key));
  }

  public List<JsonValue> jsonGet(String key, JsonPath... paths) {
    return execute(commands -> commands.jsonGet(key, paths));
  }

  public void jsonDel(String key) {
    executeVoid(commands -> commands.jsonDel(key));
  }

  public void jsonDel(String key, JsonPath path) {
    executeVoid(commands -> commands.jsonDel(key, path));
  }

  public List<JsonType> jsonType(String key, JsonPath path) {
    return execute(commands -> commands.jsonType(key, path));
  }

  public <T> T execute(Function<RedisModulesCommands<String, String>, T> action) {
    StatefulRedisModulesConnection<String, String> connection = null;
    try {
      connection = pool.borrowObject();
      RedisModulesCommands<String, String> commands = connection.sync();
      return action.apply(commands);
    } catch (Exception e) {
      throw new RuntimeException("Redis operation failed", e);
    } finally {
      if (connection != null) {
        pool.returnObject(connection);
      }
    }
  }

  public void executeVoid(java.util.function.Consumer<RedisModulesCommands<String, String>> action) {
    execute(commands -> {
      action.accept(commands);
      return null;
    });
  }
}
