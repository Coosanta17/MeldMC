package net.coosanta.meldmc.minecraft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {
    private static final Map<String, GameInstance> instances = new ConcurrentHashMap<>();

    public static Map<String, GameInstance> getInstances() {
        return instances;
    }

    // FIXME on server it could not connect to: Exception in thread "JavaFX Application Thread" java.lang.NullPointerException: Cannot invoke "net.coosanta.meldmc.minecraft.GameInstance.isMeldCached()" because the return value of "net.coosanta.meldmc.minecraft.InstanceManager.getInstance(String)" is null at net.coosanta.meldmc.gui.controllers.serverselection.ButtonPanel.serverSelected(ButtonPanel.java:89) at net.coosanta.meldmc.gui.controllers.serverselection.SelectionPanel.selectEntry(SelectionPanel.java:64) at net.coosanta.meldmc.gui.controllers.serverselection.CentrePanel.lambda<span>getServersFromManager</span>0(CentrePanel.java:66) at javafx.base/com.sun.javafx.event.CompositeEventHandler.dispatchBubblingEvent(CompositeEventHandler.java:86) at
    public static GameInstance getInstance(String address) {
        return instances.get(address);
    }

    public static void newInstance(String address) {
        instances.putIfAbsent(address, new GameInstance(address));
    }
}
