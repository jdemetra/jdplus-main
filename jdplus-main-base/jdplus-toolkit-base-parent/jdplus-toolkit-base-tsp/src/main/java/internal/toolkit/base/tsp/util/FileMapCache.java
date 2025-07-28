package internal.toolkit.base.tsp.util;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Map;

@lombok.Getter
@lombok.RequiredArgsConstructor
public final class FileMapCache<K, V> implements MapCache<K, V> {

    private final @NonNull Map<K, V> map;

    private final @NonNull File file;

    private long lastModified = Long.MIN_VALUE;

    @Override
    public void put(@NonNull K key, @NonNull V value) {
        map.put(key, value);
    }

    @Override
    public @Nullable V get(@NonNull K key) {
        V result = map.get(key);
        if (result == null) {
            return null;
        }
        long timInMillis = file.lastModified();
        if (timInMillis != lastModified) {
            lastModified = timInMillis;
            map.clear();
            return null;
        }
        return result;
    }
}
