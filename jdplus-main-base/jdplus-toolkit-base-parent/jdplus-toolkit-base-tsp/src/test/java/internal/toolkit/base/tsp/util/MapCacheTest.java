package internal.toolkit.base.tsp.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class MapCacheTest {

    @Test
    public void testCache() {
        AtomicLong ticker = new AtomicLong(0);
        Duration ttl = Duration.ofMillis(10);

        TtlMapCache<String, Integer> cache = new TtlMapCache<>(new HashMap<>(), ticker::get, ttl);

        String key = "key1";
        Integer value = 1;

        cache.put(key, value);
        assertThat(cache.getMap()).containsKey(key);
        assertThat(cache.get(key)).isEqualTo(value);

        ticker.addAndGet(ttl.toNanos());
        assertThat(cache.getMap()).containsKey(key);
        assertThat(cache.get(key)).isNull();
        assertThat(cache.getMap()).isEmpty();
    }
}
