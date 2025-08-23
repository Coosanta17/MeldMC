package net.coosanta.meldmc.minecraft.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

/**
 * Evaluates rules for libraries, arguments, and features based on OS and feature conditions
 */
public class RuleEvaluator {

    public RuleEvaluator() {
    }

    /**
     * Check if a library should be included based on its rules
     */
    public boolean passesOsRule(ObjectNode lib) {
        if (!lib.has("rules")) return true;

        for (JsonNode ruleElement : lib.get("rules")) {
            ObjectNode rule = (ObjectNode) ruleElement;
            boolean action = "allow".equals(rule.get("action").asText());

            if (rule.has("os") && matchesCurrentOS((ObjectNode) rule.get("os"))) {
                return action;
            }
            if (!rule.has("os") && !rule.has("features")) {
                return action;
            }
        }
        return false;
    }

    /**
     * Check if rules pass based on OS and LaunchArgs features
     */
    public boolean passesRule(ObjectNode rule, LaunchArgs launchArgs) {
        boolean action = "allow".equals(rule.get("action").asText());

        if (rule.has("os") && matchesCurrentOS((ObjectNode) rule.get("os"))) {
            return action;
        }
        if (rule.has("features") && matchesFeatures((ObjectNode) rule.get("features"), launchArgs)) {
            return action;
        }
        if (!rule.has("os") && !rule.has("features")) {
            return action;
        }

        return false;
    }

    private boolean matchesCurrentOS(ObjectNode osRule) {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        if (osRule.has("name")) {
            String requiredOS = osRule.get("name").asText().toLowerCase();
            boolean osMatch = switch (requiredOS) {
                case "windows" -> osName.contains("win");
                case "osx" -> osName.contains("mac");
                case "linux" -> osName.contains("linux");
                default -> osName.contains(requiredOS);
            };

            if (!osMatch) return false;
        }

        if (osRule.has("arch")) {
            String requiredArch = osRule.get("arch").asText().toLowerCase();
            return osArch.contains(requiredArch);
        }

        return true;
    }

    private boolean matchesFeatures(ObjectNode featuresRule, LaunchArgs launchArgs) {
        Iterator<String> fieldNames = featuresRule.fieldNames();
        while (fieldNames.hasNext()) {
            String featureName = fieldNames.next();
            boolean required = featuresRule.get(featureName).asBoolean();
            boolean actual = getFeatureFromLaunchArgs(featureName, launchArgs);

            if (required != actual) {
                return false;
            }
        }
        return true;
    }

    private boolean getFeatureFromLaunchArgs(String featureName, LaunchArgs launchArgs) {
        return switch (featureName) {
            case "is_demo_user" -> launchArgs.isDemo();
            case "has_custom_resolution" -> launchArgs.getWidth() != null && launchArgs.getHeight() != null;
            case "has_quick_plays_support" -> launchArgs.getQuickPlayPath() != null;
            case "is_quick_play_singleplayer" -> launchArgs.getQuickPlaySingleplayer() != null;
            case "is_quick_play_multiplayer" -> launchArgs.getQuickPlayMultiplayer() != null;
            case "is_quick_play_realms" -> launchArgs.getQuickPlayRealms() != null;
            default -> false;
        };
    }
}
