package internal.toolkit.base.tsp.util;

import jdplus.toolkit.base.tsp.util.ShortLivedCache;
import jdplus.toolkit.base.tsp.util.ShortLivedCaching;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.function.LongSupplier;

public final class MapCaching implements ShortLivedCaching {

    public MapCaching() {
        this(System::nanoTime);
    }

    private final LongSupplier ticker;

    @VisibleForTesting
    MapCaching(@NonNull LongSupplier ticker) {
        this.ticker = ticker;
    }

    @Override
    public @NonNull String getId() {
        return "hashmap";
    }

    @Override
    public <K, V> @NonNull ShortLivedCache<K, V> ofTtl(@NonNull Duration ttl) {
        return new TtlMapCache<>(new HashMap<>(), ticker, ttl);
    }

    @Override
    public @NonNull <K, V> ShortLivedCache<K, V> ofFile(@NonNull File file) {
        return new FileMapCache<>(new HashMap<>(), file);
    }
}
