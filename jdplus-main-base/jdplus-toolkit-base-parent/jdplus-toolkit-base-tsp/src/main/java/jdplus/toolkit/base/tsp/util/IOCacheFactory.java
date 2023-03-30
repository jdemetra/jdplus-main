package jdplus.toolkit.base.tsp.util;

import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.time.Duration;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        singleton = true,
        noFallback = true
)
public interface IOCacheFactory {

    <K, V> @NonNull IOCache<K, V> ofTtl(@NonNull Duration ttl);

    <K, V> @NonNull IOCache<K, V> ofFile(@NonNull File file);
}
