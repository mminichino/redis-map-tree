package com.codelry.redis.maptree;

import com.codelry.redis.maptree.service.JsonFlattener;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMapTree {
  private static final Logger logger = LoggerFactory.getLogger(TestMapTree.class);

  @ParameterizedTest
  @ValueSource(strings = {"test.json"})
  public void testRecordGeneration(String templateFile) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();

    try {
      Reader reader = new InputStreamReader(Objects.requireNonNull(loader.getResourceAsStream(templateFile)));
      JsonElement jsonElement = JsonParser.parseReader(reader);

      logger.info("-- Paths ---");
      Set<String> paths = JsonFlattener.flattenTree(jsonElement);
      for (String path : paths) {
        logger.info(path);
      }

      logger.info("-- Tree ---");
      Map<String, Object> tree = JsonFlattener.mapPathTree(jsonElement);
      for (Map.Entry<String, Object> entry : tree.entrySet()) {
        logger.info("{}: {}", entry.getKey(), entry.getValue());
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }
}
