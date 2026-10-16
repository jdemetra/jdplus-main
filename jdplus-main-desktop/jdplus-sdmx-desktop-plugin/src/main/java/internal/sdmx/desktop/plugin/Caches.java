package internal.sdmx.desktop.plugin;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import jdplus.toolkit.base.tsp.util.ShortLivedCache;
import jdplus.toolkit.base.tsp.util.ShortLivedCachingLoader;

public final class Caches {

    private Caches() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <K, V> ConcurrentMap<K, V> asConcurrentMap(ShortLivedCache<K, V> cache) {
        return new ConcurrentMap<K, V>() {
            @Override
            public V putIfAbsent(K key, V value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object key, Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean replace(K key, V oldValue, V newValue) {
                throw new UnsupportedOperationException();
            }

            @Override
            public V replace(K key, V value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isEmpty() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsKey(Object key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsValue(Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public V get(Object key) {
                return cache.get((K) key);
            }

            @Override
            public V put(K key, V value) {
                cache.put(key, value);
                return null;
            }

            @Override
            public V remove(Object key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putAll(Map<? extends K, ? extends V> m) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<K> keySet() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<V> values() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<Entry<K, V>> entrySet() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <K, V> ConcurrentMap<K, V> ttlCacheAsMap(Duration duration) {
        return asConcurrentMap(ShortLivedCachingLoader.get().ofTtl(duration));
    }
}
