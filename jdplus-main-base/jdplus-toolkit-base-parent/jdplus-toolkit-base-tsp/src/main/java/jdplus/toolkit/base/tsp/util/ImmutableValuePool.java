package jdplus.toolkit.base.tsp.util;

import jdplus.toolkit.base.tsp.DataSource;
import nbbrd.design.ThreadSafe;
import nbbrd.io.function.IOFunction;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ThreadSafe
@lombok.AllArgsConstructor
public final class ImmutableValuePool<VALUE> {

    @NonNull
    public static <VALUE> ImmutableValuePool<VALUE> of() {
        return new ImmutableValuePool<>(new ConcurrentHashMap<>());
    }

    @lombok.NonNull
    private final ConcurrentMap<DataSource, VALUE> resources;

    public @NonNull VALUE get(@NonNull DataSource dataSource, @NonNull ImmutableValueFactory<VALUE> delegate) throws IOException {
        try {
            return resources.computeIfAbsent(dataSource, IOFunction.unchecked(delegate::load));
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    public void remove(DataSource dataSource) {
        resources.remove(dataSource);
    }

    public void clear() {
        resources.clear();
    }

    public @Nullable VALUE peek(@NonNull DataSource dataSource) {
        return resources.get(dataSource);
    }

    public @NonNull ImmutableValueFactory<VALUE> asFactory(@NonNull ImmutableValueFactory<VALUE> delegate) {
        Objects.requireNonNull(delegate);
        return dataSource -> get(dataSource, delegate);
    }
}
