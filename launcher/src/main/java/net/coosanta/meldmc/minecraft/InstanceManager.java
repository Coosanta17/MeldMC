package net.coosanta.meldmc.minecraft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {
    private static final Map<String, GameInstance> instances = new ConcurrentHashMap<>();

    public static Map<String, GameInstance> getInstances() {
        return instances;
    }

    public static GameInstance getInstance(String address) {
        return instances.get(address);
    }

    public static void newInstance(String address) {
        instances.putIfAbsent(address, new GameInstance(address));
    }
}
