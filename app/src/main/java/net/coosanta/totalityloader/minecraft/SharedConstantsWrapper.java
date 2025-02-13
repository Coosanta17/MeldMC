package net.coosanta.totalityloader.minecraft;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SharedConstantsWrapper extends ReflectionWrapper {
    private final Object instance;
    private final String CLASS_NAME = "net/minecraft/SharedConstants";

    public SharedConstantsWrapper() throws ClassNotFoundException, IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        super();

        instance = getClassFromMappings(CLASS_NAME);
    }

    @Override
    public Object callMethod(String deobfuscatedMethodName, Object... args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }

        Class<?> targetClass = (Class<?>) getInstance();
        Method method = targetClass.getMethod(deobfuscatedMethodName, parameterTypes);
        return method.invoke(null, args); // invoke as a static method
    }

    @Override
    public Object getInstance() {
        return instance;
    }
}
