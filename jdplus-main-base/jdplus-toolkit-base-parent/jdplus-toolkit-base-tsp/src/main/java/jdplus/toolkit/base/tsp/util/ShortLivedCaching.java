package jdplus.toolkit.base.tsp.util;

import internal.toolkit.base.tsp.util.MapCaching;
import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;

import java.io.File;
import java.time.Duration;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        singleton = true,
        fallback = MapCaching.class
)
@ThreadSafe
public interface ShortLivedCaching {

    @ServiceId
    @NonNull String getId();

    <K, V> @NonNull ShortLivedCache<K, V> ofTtl(@NonNull Duration ttl);

    <K, V> @NonNull ShortLivedCache<K, V> ofFile(@NonNull File file);
}
