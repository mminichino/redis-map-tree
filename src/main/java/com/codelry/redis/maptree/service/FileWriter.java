package com.codelry.redis.maptree.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class FileWriter {

  private FileWriter() {}

  public static void create(Set<String> set) {

    String filePath = "output.txt";

    try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8)) {
      for (String s : set) {
        writer.println(s);
      }
      System.out.println("Set of strings successfully written to " + filePath);
    } catch (IOException e) {
      System.err.println("Error writing to file: " + e.getMessage());
    }
  }
}
