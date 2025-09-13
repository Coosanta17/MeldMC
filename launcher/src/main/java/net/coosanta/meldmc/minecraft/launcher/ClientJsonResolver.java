package net.coosanta.meldmc.minecraft.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.coosanta.meldmc.exceptions.ClientJsonNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles loading and caching of client JSON configurations
 */
public class ClientJsonResolver {
    private static final Logger log = LoggerFactory.getLogger(ClientJsonResolver.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, ObjectNode> versionCache = new ConcurrentHashMap<>();
    private final Path versionsDir;

    public ClientJsonResolver(Path versionsDir) {
        this.versionsDir = versionsDir;
    }

    /**
     * Load client JSON with version inheritance resolution.
     */
    public ObjectNode loadClientJson(String id) throws IOException, ClientJsonNotFoundException {
        Path clientJson = findClientJson(id);
        if (!Files.exists(clientJson)) {
            throw new ClientJsonNotFoundException("No client.json found for version: " + id);
        }

        return loadAndResolveClientJson(clientJson);
    }

    private Path findClientJson(String id) throws IOException, ClientJsonNotFoundException {
        Path clientJson = versionsDir.resolve(id).resolve(id + ".json");
        if (Files.exists(clientJson)) {
            return clientJson;
        }

        for (Map<String, String> version : loadVersionsManifest()) {
            if (id.equals(version.get("id"))) {
                String url = version.get("url");
                Path versionDir = versionsDir.resolve(id);
                Files.createDirectories(versionDir);
                Path targetJson = versionDir.resolve(id + ".json");
                FileDownloader.downloadFile(url, targetJson);
                return targetJson;
            }
        }
        throw new ClientJsonNotFoundException("No client.json found for version: " + id);
    }

    private Set<Map<String, String>> loadVersionsManifest() throws IOException {
        Path manifestFile = versionsDir.resolve("version_manifest_v2.json");
        if (!Files.exists(manifestFile)) {
            throw new IllegalStateException("No version manifest found at: " + manifestFile);
        }
        JsonNode versionsArray = loadJsonFile(manifestFile).get("versions");
        Set<Map<String, String>> versions = new HashSet<>();
        for (JsonNode elem : versionsArray) {
            Map<String, String> map = new HashMap<>();
            elem.fieldNames().forEachRemaining(fieldName -> map.put(fieldName, elem.get(fieldName).asText()));
            versions.add(map);
        }
        return versions;
    }

    private ObjectNode loadAndResolveClientJson(Path clientJson) throws IOException, ClientJsonNotFoundException {
        ObjectNode clientData = loadJsonFile(clientJson);

        // Resolve inheritance
        if (clientData.has("inheritsFrom")) {
            String parentId = clientData.get("inheritsFrom").asText();
            ObjectNode parentData = loadJsonFile(findClientJson(parentId));
            if (parentData != null) {
                return JsonMerger.merge(parentData, clientData);
            }
        }

        return clientData;
    }

    private ObjectNode loadJsonFile(Path jsonFile) throws IOException {
        String content = Files.readString(jsonFile);
        return (ObjectNode) objectMapper.readTree(content);
    }
}
