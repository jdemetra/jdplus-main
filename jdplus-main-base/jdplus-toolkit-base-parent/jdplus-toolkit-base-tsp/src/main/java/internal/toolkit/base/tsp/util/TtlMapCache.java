package internal.toolkit.base.tsp.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nbbrd.design.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.function.LongSupplier;

@lombok.Getter
@RequiredArgsConstructor
public final class TtlMapCache<K, V> implements MapCache<K, V> {

    private final @NonNull Map<K, Entry<V>> map;

    private final @NonNull LongSupplier ticker;

    private final @NonNull Duration ttl;

    @Override
    public void put(@NonNull K key, @NonNull V value) {
        map.put(key, new Entry<>(value, ticker.getAsLong()));
    }

    @Override
    public @Nullable V get(@NonNull K key) {
        Entry<V> result = map.get(key);
        if (result == null) {
            return null;
        }
        if (!validate(result.creationTime())) {
            map.remove(key);
            return null;
        }
        return result.value();
    }

    private boolean validate(long creationTime) {
        return ticker.getAsLong() < creationTime + ttl.toNanos();
    }

    @VisibleForTesting
    public record Entry<V>(@NonNull V value, long creationTime) {
    }
}
