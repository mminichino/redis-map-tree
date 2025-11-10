package com.codelry.redis.maptree.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonFlattener {

    private static final Logger logger = LoggerFactory.getLogger(JsonFlattener.class);
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

    public static Map<String, String> mapPaths(byte[] json) {
        JsonElement root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8));
        return mapPaths(root);
    }

    public static Map<String, String> mapPaths(JsonElement root) {
        Map<String, String> map = new HashMap<>();
        map(root, "", map);
        return map;
    }

    public static Set<String> flattenTree(JsonElement root) {
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        walkTree(root, "", paths);
        return paths;
    }

    public static Set<String> flattenTree(byte[] json) {
        JsonElement root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8));
        return flattenTree(root);
    }

    public static Map<String, Object> mapPathTree(byte[] json) {
        JsonElement root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8));
        return mapPathTree(root);
    }

    public static Map<String, Object> mapPathTree(JsonElement root) {
        Map<String, Object> tree = new HashMap<>();
        mapTree(root, "", tree);
        return tree;
    }

    private static void walk(JsonElement element, String prefix, Set<String> out) {
        if (element == null || element.isJsonNull()) {
            out.add(prefix);
            return;
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String nextPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                walk(entry.getValue(), nextPrefix, out);
            }
            return;
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                String nextPrefix = prefix + "[" + i + "]";
                walk(array.get(i), nextPrefix, out);
            }
            return;
        }

        out.add(prefix);
    }

    private static void map(JsonElement element, String prefix, Map<String, String> map) {
        if (element == null) {
            return;
        }

        if (element.isJsonNull()) {
            map.put(prefix, "__null__");
            return;
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String nextPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                map(entry.getValue(), nextPrefix, map);
            }
            return;
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                String nextPrefix = prefix + "[" + i + "]";
                map(array.get(i), nextPrefix, map);
            }
            return;
        }

        map.put(prefix, element.getAsString());
    }

    private static void walkTree(JsonElement element, String prefix, Set<String> out) {
        if (element == null) {
            return;
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            String key = prefix.isEmpty() ? "root" : prefix;
            out.add(key);

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String nextPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                walkTree(entry.getValue(), nextPrefix, out);
            }
            return;
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            if (!array.isEmpty() && !array.get(0).isJsonObject()) {
                String key = prefix.isEmpty() ? "root" : prefix;
                out.add(key);
            }

            for (int i = 0; i < array.size(); i++) {
                String nextPrefix = prefix + "[" + i + "]";
                walkTree(array.get(i), nextPrefix, out);
            }
        }
    }

    private static void mapTree(JsonElement element, String prefix, Map<String, Object> tree) {
        if (element == null) {
            return;
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String nextPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                mapTree(entry.getValue(), nextPrefix, tree);
            }
            return;
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                JsonElement item = array.get(i);
                String nextPrefix;
                if (item.isJsonPrimitive()) {
                    nextPrefix = prefix + ".__list_item__";
                } else {
                    nextPrefix = prefix + "[" + i + "]";
                }
                mapTree(item, nextPrefix, tree);
            }
            return;
        }

        String[] path = prefix.split("\\.");
        String key = path.length == 1 ? "root" : prefix.substring(0, prefix.lastIndexOf('.'));
        String field = path[path.length - 1];
        if (tree.containsKey(key)) {
            Object value = tree.get(key);
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                String fieldValue = element.isJsonNull() ? "__null__" : element.getAsString();
                mapValue.put(field, fieldValue);
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> listValue = (List<String>) value;
                listValue.add(element.getAsString());
            } else {
                logger.warn("Unexpected value type for key: {} -> {}", key, value);
            }
        } else {
            if (field.equals("__list_item__")) {
                List<String> listValue = new ArrayList<>();
                listValue.add(element.getAsString());
                tree.put(key, listValue);
            } else {
                Map<String, Object> mapValue = new HashMap<>();
                mapValue.put(field, element.getAsString());
                tree.put(key, mapValue);
            }
        }
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
