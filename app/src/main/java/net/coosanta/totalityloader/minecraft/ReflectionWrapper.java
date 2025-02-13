package net.coosanta.totalityloader.minecraft;

import net.fabricmc.mappingio.tree.MappingTree;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ReflectionWrapper {
    private final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private final Map<String, Field> fieldCache = new ConcurrentHashMap<>();

    protected final MappingTree mappings;
    protected String className;

    protected ReflectionWrapper() {
        this.mappings = MinecraftClasses.getMappings();
    }

    public abstract Object getInstance();

    public MappingTree getMappings() {
        return this.mappings;
    }

    public String getClassName() {
        return className;
    }

    protected Class<?> getClassFromMappings(String name) throws ClassNotFoundException {
        MappingTree.ClassMapping classMapping = mappings.getClass(name);
        if (classMapping == null) {
            throw new ClassNotFoundException("No mapping found for class: " + name);
        }

        Class<?> clazz = classCache.get(name);
        if (clazz == null) {
            clazz = Class.forName(classMapping.getName(MinecraftClasses.SRC_NAMESPACE));
            classCache.put(name, clazz);
        }
        return clazz;
    }

    /**
     * Calls an obfuscated method reflectively from mappings.
     * @param deobfuscatedMethodName
     * Format it as "com/example/Class"
     * @param args
     * Parameters the method uses.
     * @return Return object of the method.
     * @throws NoSuchMethodException If no mapping or method is found.
     * @throws InvocationTargetException An exception wrapper if the underlying method throws an exception.
     * @throws IllegalAccessException If it tries to call a method it doesn't have access to.
     */
    public Object callMethod(String deobfuscatedMethodName, Object... args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?>[] paramTypes = Arrays.stream(args)
                .map(arg -> arg == null ? null : arg.getClass())
                .toArray(Class<?>[]::new);

        String descriptor = generateDescriptor(args);

        MappingTree.MethodMapping methodMapping =
                getMappings().getMethod(getClassName(), deobfuscatedMethodName, descriptor);

        if (methodMapping == null) {
            throw new NoSuchMethodException("No mapping found for method: " + deobfuscatedMethodName);
        }

        // Get obfuscated method name
        String obfuscatedMethodName = methodMapping.getDstName(getMappings().getNamespaceId(MinecraftClasses.DST_NAMESPACE));

        if (obfuscatedMethodName == null) {
            throw new NoSuchMethodException("Failed to find obfuscated method name from method mapping entry for method: " + deobfuscatedMethodName);
        }

        String cacheKey = obfuscatedMethodName + "#" + Arrays.toString(paramTypes);
        Method method = methodCache.get(cacheKey);

        if (method == null) {
            method = getInstance().getClass().getMethod(obfuscatedMethodName, paramTypes);
            methodCache.put(cacheKey, method);
        }

        // Invoke the method
        return method.invoke(getInstance(), args);
    }

    /**
     * Calls an obfuscated static method reflectively from mappings.
     * @param deobfuscatedMethodName The deobfuscated name of the method.
     * @param args The parameters the method uses.
     * @return The return object of the method.
     * @throws NoSuchMethodException If no mapping or method is found.
     * @throws InvocationTargetException An exception wrapper if the underlying method throws an exception.
     * @throws IllegalAccessException If it cannot access the method.
     */
    public Object callStaticMethod(String deobfuscatedMethodName, Object... args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // Infer parameter types; note: if any argument is null, this will yield null in the array.
        Class<?>[] paramTypes = Arrays.stream(args)
                .map(arg -> arg == null ? null : arg.getClass())
                .toArray(Class<?>[]::new);

        String descriptor = generateDescriptor(args);

        MappingTree.MethodMapping methodMapping =
                getMappings().getMethod(getClassName(), deobfuscatedMethodName, descriptor);

        if (methodMapping == null) {
            throw new NoSuchMethodException("No mapping found for method: " + deobfuscatedMethodName);
        }

        // Get obfuscated method name from the mapping
        String obfuscatedMethodName = methodMapping.getDstName(getMappings().getNamespaceId(MinecraftClasses.DST_NAMESPACE));

        if (obfuscatedMethodName == null) {
            throw new NoSuchMethodException("Failed to find obfuscated method name for: " + deobfuscatedMethodName);
        }

        String cacheKey = obfuscatedMethodName + "#" + Arrays.toString(paramTypes);
        Method method = methodCache.get(cacheKey);

        if (method == null) {
            method = getInstance().getClass().getMethod(obfuscatedMethodName, paramTypes);
            methodCache.put(cacheKey, method);
        }

        // For static methods, the target object is null.
        return method.invoke(null, args);
    }

    /**
     * Gets the value of an obfuscated field reflectively from mappings.
     * @param deobfuscatedFieldName
     * Format it as "com/example/Class"
     * @return Field value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public Object getFieldValue(String deobfuscatedFieldName) throws NoSuchFieldException, IllegalAccessException {
        MappingTree.FieldMapping fieldMapping =
                getMappings().getField(getClassName(), deobfuscatedFieldName, null);

        if (fieldMapping == null) {
            throw new NoSuchFieldException("No mapping found for field: " + deobfuscatedFieldName);
        }

        // Get obfuscated field name
        String obfuscatedFieldName = fieldMapping.getDstName(
                getMappings().getNamespaceId(MinecraftClasses.DST_NAMESPACE)
        );

        if (obfuscatedFieldName == null) {
            throw new NoSuchFieldException("Failed to find obfuscated field name from field mapping entry for field: " + deobfuscatedFieldName);
        }

        Field field = fieldCache.get(obfuscatedFieldName);

        if (field == null) {
            field = getInstance().getClass().getField(obfuscatedFieldName);
            fieldCache.put(obfuscatedFieldName, field);
        }

        return field.get(getInstance());
    }

    // I was lazy, so I did this instead of finding something that already does it.
    private static String generateDescriptor(Object... args) {
        StringBuilder descriptor = new StringBuilder("(");
        for (Object arg : args) {
            descriptor.append(getJVMType(arg.getClass()));
        }
        descriptor.append(")"); // Ignoring return types for now.
        return descriptor.toString();
    }

    private static String getJVMType(Class<?> clazz) {
        if (clazz.isArray()) {
            return "[" + getJVMType(clazz.getComponentType());
        }
        return switch (clazz.getName()) {
            case "int", "java.lang.Integer" -> "I";
            case "long", "java.lang.Long" -> "J";
            case "double", "java.lang.Double" -> "D";
            case "float", "java.lang.Float" -> "F";
            case "boolean", "java.lang.Boolean" -> "Z";
            case "char", "java.lang.Character" -> "C";
            case "short", "java.lang.Short" -> "S";
            case "byte", "java.lang.Byte" -> "B";
            case "void", "java.lang.Void" -> "V";
            default -> "L" + clazz.getName().replace('.', '/') + ";"; // Object types
        };
    }
}
