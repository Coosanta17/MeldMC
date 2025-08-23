package net.coosanta.meldmc.minecraft.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

/**
 * Handles merging of parent and child JSON configurations for inheritance
 */
public class JsonMerger {

    /**
     * Merge parent JSON into child JSON, with child properties taking precedence
     */
    public static ObjectNode merge(ObjectNode parent, ObjectNode child) {
        ObjectNode merged = parent.deepCopy();

        // Override or add child properties with special handling for certain keys
        Iterator<String> fieldNames = child.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            JsonNode childValue = child.get(key);

            switch (key) {
                case "libraries" -> mergeLibraries(merged, childValue);
                case "arguments" -> mergeArguments(merged, childValue);
                default -> merged.set(key, childValue); // Child overrides parent
            }
        }

        return merged;
    }

    private static void mergeLibraries(ObjectNode merged, JsonNode childLibraries) {
        if (!merged.has("libraries") || !childLibraries.isArray()) {
            merged.set("libraries", childLibraries);
            return;
        }

        ArrayNode parentLibs = (ArrayNode) merged.get("libraries");
        ArrayNode childLibs = (ArrayNode) childLibraries;
        ArrayNode mergedLibs = merged.arrayNode();

        for (JsonNode lib : parentLibs) {
            mergedLibs.add(lib);
        }

        for (JsonNode lib : childLibs) {
            mergedLibs.add(lib);
        }

        merged.set("libraries", mergedLibs);
    }

    private static void mergeArguments(ObjectNode merged, JsonNode childArguments) {
        if (!merged.has("arguments") || !childArguments.isObject()) {
            merged.set("arguments", childArguments);
            return;
        }

        ObjectNode parentArgs = (ObjectNode) merged.get("arguments");
        ObjectNode childArgs = (ObjectNode) childArguments;
        ObjectNode mergedArgs = merged.objectNode();

        Iterator<String> parentFieldNames = parentArgs.fieldNames();
        while (parentFieldNames.hasNext()) {
            String argType = parentFieldNames.next();
            mergedArgs.set(argType, parentArgs.get(argType));
        }

        Iterator<String> childFieldNames = childArgs.fieldNames();
        while (childFieldNames.hasNext()) {
            String argType = childFieldNames.next();
            JsonNode childArgValue = childArgs.get(argType);

            if (mergedArgs.has(argType) && childArgValue.isArray()) {
                ArrayNode parentArgArray = (ArrayNode) mergedArgs.get(argType);
                ArrayNode childArgArray = (ArrayNode) childArgValue;
                ArrayNode mergedArgArray = merged.arrayNode();

                for (JsonNode arg : parentArgArray) {
                    mergedArgArray.add(arg);
                }
                for (JsonNode arg : childArgArray) {
                    mergedArgArray.add(arg);
                }

                mergedArgs.set(argType, mergedArgArray);
            } else {
                mergedArgs.set(argType, childArgValue);
            }
        }

        merged.set("arguments", mergedArgs);
    }
}
