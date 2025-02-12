package net.coosanta.totalityloader.minecraft;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ReflectionWrapper {
    protected final MinecraftClasses mappings;

    protected ReflectionWrapper() throws IOException {
        this.mappings = MinecraftClasses.getInstance();
    }

    public abstract Object getInstance();

    public MinecraftClasses getMappings() {
        return this.mappings;
    }

    public Object callMethod(String deobfuscatedMethodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        Method method = getMappings().getObfuscatedMethod(getInstance().getClass(), deobfuscatedMethodName, parameterTypes);
        return method.invoke(getInstance(), args);
    }

    public Object getFieldValue(String deobfuscatedFieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = getMappings().getObfuscatedField(getInstance().getClass(), deobfuscatedFieldName);
        return field.get(getInstance());
    }
}
