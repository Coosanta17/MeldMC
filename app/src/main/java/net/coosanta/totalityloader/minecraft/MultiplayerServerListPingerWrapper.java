package net.coosanta.totalityloader.minecraft;

import java.lang.reflect.InvocationTargetException;

public class MultiplayerServerListPingerWrapper extends ReflectionWrapper {
    Object instance;

    public MultiplayerServerListPingerWrapper() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super("net/minecraft/client/network/MultiplayerServerListPinger");

        Class<?> multiplayerServerListPinger = getClassFromMappings(getDeobfuscatedClassName());

        instance = multiplayerServerListPinger.getDeclaredConstructor().newInstance();
    }

    @Override
    public Object getInstance() {
        return instance;
    }
}
