package net.unit8.kysymys.scorer;

import org.reflections8.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class SutDetector {
    public static <T> T detect(Class<T> iface) {
        String packageName = iface.getPackageName();
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends T>> impls = reflections.getSubTypesOf(iface);
        Class<? extends T> clazz = impls.stream().findFirst().orElseThrow();
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new AssertionError("Instantiation error while creating a SUT", e);
        }
    }
}
