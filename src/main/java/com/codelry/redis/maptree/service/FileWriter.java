package com.codelry.redis.maptree.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWriter {

  private static final Logger logger = LoggerFactory.getLogger(FileWriter.class);
  private FileWriter() {}

  public static void create(Set<String> set) {
    String file = "output.txt";
    create(set, file);
  }

  public static void create(Set<String> set, String filePath) {
    try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8)) {
      for (String s : set) {
        writer.println(s);
      }
      logger.info("Set of strings successfully written to {}", filePath);
    } catch (IOException e) {
      logger.error("Error writing to file: {}", e.getMessage());
    }
  }
}
