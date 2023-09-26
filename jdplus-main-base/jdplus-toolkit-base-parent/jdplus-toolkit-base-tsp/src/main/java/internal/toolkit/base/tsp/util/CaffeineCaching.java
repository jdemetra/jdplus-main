package internal.toolkit.base.tsp.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import jdplus.toolkit.base.tsp.util.ShortLivedCache;
import jdplus.toolkit.base.tsp.util.ShortLivedCaching;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;

import java.io.File;
import java.time.Duration;

@ServiceProvider
public final class CaffeineCaching implements ShortLivedCaching {

    public CaffeineCaching() {
        this(Ticker.systemTicker());
    }

    private final @NonNull Ticker ticker;

    @VisibleForTesting
    CaffeineCaching(@NonNull Ticker ticker) {
        this.ticker = ticker;
    }

    @Override
    public @NonNull String getId() {
        return "caffeine";
    }

    @Override
    public @NonNull <K, V> ShortLivedCache<K, V> ofTtl(@NonNull Duration ttl) {
        Cache<K, V> result = Caffeine
                .newBuilder()
                .ticker(ticker)
                .expireAfterWrite(ttl)
                .softValues()
                .build();
        return new SimpleMapCache<>(result.asMap());
    }

    @Override
    public @NonNull <K, V> ShortLivedCache<K, V> ofFile(@NonNull File file) {
        Cache<K, V> result = Caffeine
                .newBuilder()
                .ticker(ticker)
                .softValues()
                .build();
        return new FileMapCache<>(result.asMap(), file);
    }
}
