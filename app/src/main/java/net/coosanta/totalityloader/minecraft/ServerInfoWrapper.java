package net.coosanta.totalityloader.minecraft;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ServerInfoWrapper extends ReflectionWrapper {
    private final Object instance;

    public ServerInfoWrapper(String name, String address, String serverTypeName)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        super();

        Class<?> serverInfoClass = mappings.getObfuscatedClass("net/minecraft/client/network/ServerInfo");
        Class<?> rawServerTypeClass = mappings.getObfuscatedClass("net/minecraft/client/network/ServerInfo$ServerType");

        if (!rawServerTypeClass.isEnum()) {
            throw new IllegalStateException("ServerType is not an enum! Possible wrong class called.");
        }

        // Properly cast to an Enum class
        @SuppressWarnings("unchecked")
        Class<? extends Enum<?>> serverTypeEnum = (Class<? extends Enum<?>>) rawServerTypeClass;

        Enum<?> serverTypeInstance = Enum.valueOf(serverTypeEnum.asSubclass(Enum.class), serverTypeName);

        // Create an instance of ServerInfo with the enum
        instance = serverInfoClass.getDeclaredConstructor(String.class, String.class, serverTypeEnum)
                .newInstance(name, address, serverTypeInstance);

        System.out.println("Created ServerInfo instance: " + instance);
    }

    @Override
    public Object getInstance() {
        return instance;
    }
}