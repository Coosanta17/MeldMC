package net.coosanta.totalityloader.minecraft;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SharedConstantsWrapper extends ReflectionWrapper {
    private final Object instance;

    public SharedConstantsWrapper() throws ClassNotFoundException, IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        super("net/minecraft/SharedConstants");

        instance = getClassFromMappings(deobfuscatedClassName);
    }

    @Override
    public Object getInstance() {
        return instance;
    }
}
