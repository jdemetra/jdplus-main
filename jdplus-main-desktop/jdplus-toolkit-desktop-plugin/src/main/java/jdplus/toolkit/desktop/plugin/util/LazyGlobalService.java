package jdplus.toolkit.desktop.plugin.util;

import lombok.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public final class LazyGlobalService {

    private LazyGlobalService() {
        // static class
    }

    private static final ConcurrentMap<Class<?>, Object> INSTANCES = new ConcurrentHashMap<>();

    public static <T> @NonNull T get(@NonNull Class<T> type, @NonNull Supplier<T> factory) {
        return type.cast(INSTANCES.computeIfAbsent(type, x -> factory.get()));
    }
}
