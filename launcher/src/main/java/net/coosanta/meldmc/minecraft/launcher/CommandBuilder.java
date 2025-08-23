package net.coosanta.meldmc.minecraft.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.coosanta.meldmc.minecraft.launcher.ClientLauncher.librariesDir;
import static net.coosanta.meldmc.minecraft.launcher.ClientLauncher.nativesDir;

/**
 * Builds the command line arguments for launching Minecraft
 */
public class CommandBuilder {
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_]+)}");

    private final RuleEvaluator ruleEvaluator;
    private final Map<String, String> placeholders = new HashMap<>();

    public CommandBuilder(RuleEvaluator ruleEvaluator) {
        this.ruleEvaluator = ruleEvaluator;
    }

    /**
     * Build the complete launch command
     */
    public List<String> buildCommand(ObjectNode clientData, List<Path> classpath, LaunchArgs launchArgs) {
        List<String> command = new ArrayList<>();

        buildPlaceholders(launchArgs, classpath);

        var javaExec = JavaLocator.javaPathFromPid();
        if (javaExec.isEmpty())
            throw new IllegalStateException("Unable to locate Java executable!!!!!!!!!!!!!!!!!!!!!");
        command.add(javaExec.get().toString());

        command.addAll(buildJvmArgs(clientData, launchArgs)); // TODO: Custom JVM args
        command.add(clientData.get("mainClass").asText());
        command.addAll(buildGameArgs(clientData, launchArgs));

        return command;
    }

    private List<String> buildJvmArgs(ObjectNode clientData, LaunchArgs launchArgs) {
        List<String> jvmArgs = Collections.emptyList();

        JsonNode jvmArgsNode = clientData.path("arguments").path("jvm");
        if (jvmArgsNode.isArray()) {
            jvmArgs = processArguments((ArrayNode) jvmArgsNode);
        }

        jvmArgs.replaceAll(s -> replaceVariables(s, launchArgs));

        return jvmArgs;
    }

    private List<String> buildGameArgs(ObjectNode clientData, LaunchArgs launchArgs) {
        if (clientData.has("arguments") && clientData.get("arguments").has("game")) {
            return processArguments((ArrayNode) clientData.get("arguments").get("game"), launchArgs);
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> processArguments(ArrayNode args, LaunchArgs launchArgs) {
        List<String> output = new ArrayList<>();
        for (JsonNode argElement : args) {
            if (argElement.isTextual()) {
                output.add(replaceVariables(argElement.asText(), launchArgs));
                continue;
            }
            if (argElement.isObject()) {
                ObjectNode argObj = (ObjectNode) argElement;
                if (argObj.has("rules")) {
                    if (passesRules(argObj, launchArgs)) {
                        JsonNode value = argObj.get("value");
                        if (value.isTextual()) {
                            output.add(replaceVariables(value.asText(), launchArgs));
                        } else if (value.isArray()) {
                            value.forEach(v -> output.add(replaceVariables(v.asText(), launchArgs)));
                        }
                    }
                }
            }
        }
        return output;
    }

    private List<String> processArguments(ArrayNode args) {
        List<String> output = new ArrayList<>();
        for (JsonNode argElement : args) {
            if (argElement.isTextual()) {
                output.add(argElement.asText());
                continue;
            }
            if (argElement.isObject()) {
                ObjectNode argObj = (ObjectNode) argElement;
                if (argObj.has("rules") && ruleEvaluator.passesOsRule(argObj)) {
                    JsonNode value = argObj.get("value");
                    if (value.isTextual()) {
                        output.add(value.asText());
                    } else if (value.isArray()) {
                        value.forEach(v -> output.add(v.asText()));
                    }
                }
            }
        }
        return output;
    }

    private boolean passesRules(ObjectNode argObj, LaunchArgs launchArgs) {
        JsonNode rules = argObj.get("rules");
        if (!rules.isArray()) return false;

        for (JsonNode rule : rules) {
            if (ruleEvaluator.passesRule((ObjectNode) rule, launchArgs)) {
                return true;
            }
        }
        return false;
    }

    private void buildPlaceholders(LaunchArgs launchArgs, List<Path> classpath) {
        placeholders.put("auth_player_name", launchArgs.getUsername());
        placeholders.put("version_name", launchArgs.getVersion());
        placeholders.put("game_directory", launchArgs.getGameDir().toString());
        placeholders.put("assets_root", launchArgs.getAssetsDir().toString());
        placeholders.put("assets_index_name", launchArgs.getAssetIndex());
        placeholders.put("auth_uuid", launchArgs.getUuid());
        placeholders.put("auth_access_token", launchArgs.getAccessToken());
        placeholders.put("clientid", launchArgs.getClientId());
        placeholders.put("auth_xuid", launchArgs.getXuid());
        placeholders.put("user_type", launchArgs.getUserType());
        placeholders.put("version_type", launchArgs.getVersionType());
        placeholders.put("resolution_width", launchArgs.getWidth().toString());
        placeholders.put("resolution_height", launchArgs.getHeight().toString());
        placeholders.put("quickPlayPath", launchArgs.getQuickPlayPath());
        placeholders.put("quickPlaySingleplayer", launchArgs.getQuickPlaySingleplayer());
        placeholders.put("quickPlayMultiplayer", launchArgs.getQuickPlayMultiplayer());
        placeholders.put("quickPlayRealms", launchArgs.getQuickPlayRealms());

        placeholders.put("launcher_name", "MeldMC");
        placeholders.put("natives_directory", nativesDir.toString());
        placeholders.put("classpath_separator", File.pathSeparator);
        placeholders.put("library_directory", librariesDir.toString());

        placeholders.put("classpath", buildClasspathString(classpath));

    }

    private String replaceVariables(String input, LaunchArgs launchArgs) {
        if (input == null || !input.contains("${")) {
            return input;
        }

        Matcher matcher = VAR_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder(input.length());
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = placeholders.get(name);
            String replacement = value != null ? value : matcher.group(0); // leave unchanged if unknown
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String buildClasspathString(List<Path> classpath) {
        return classpath.stream()
                .map(Path::toString)
                .reduce((a, b) -> a + File.pathSeparator + b)
                .orElse("");
    }
}
