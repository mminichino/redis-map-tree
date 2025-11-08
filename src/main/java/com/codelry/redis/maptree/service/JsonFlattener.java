package com.codelry.redis.maptree.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class JsonFlattener {

    private static final Gson GSON = new Gson();

    private JsonFlattener() {
        // no instances
    }

    public static Set<String> flattenPaths(byte[] json) {
        JsonElement root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8));
        return flattenPaths(root);
    }

    public static Set<String> flattenPaths(String json) {
        JsonElement root = JsonParser.parseString(json);
        return flattenPaths(root);
    }

    public static Set<String> flattenPaths(JsonElement root) {
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        walk(root, "", paths);
        return paths;
    }

    private static void walk(JsonElement element, String prefix, Set<String> out) {
        if (element == null || element.isJsonNull()) {
            out.add(prefix);
            return;
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            if (!prefix.isEmpty()) {
                out.add(prefix);
            }

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String nextPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                walk(entry.getValue(), nextPrefix, out);
            }
            return;
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            if (!prefix.isEmpty()) {
                out.add(prefix);
            }

            for (int i = 0; i < array.size(); i++) {
                String nextPrefix = prefix + "[" + i + "]";
                walk(array.get(i), nextPrefix, out);
            }
            return;
        }

        out.add(prefix);
    }

    private static boolean isTerminalObject(JsonObject obj) {
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonObject() || value.isJsonArray()) {
                return false;
            }
        }
        return true;
    }
}
