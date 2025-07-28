package internal.toolkit.base.tsp.util;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@lombok.Getter
@lombok.RequiredArgsConstructor
public final class SimpleMapCache<K, V> implements MapCache<K, V> {

    private final @NonNull Map<K, V> map;

    @Override
    public void put(@NonNull K key, @NonNull V value) {
        map.put(key, value);
    }

    @Override
    public @Nullable V get(@NonNull K key) {
        return map.get(key);
    }
}
