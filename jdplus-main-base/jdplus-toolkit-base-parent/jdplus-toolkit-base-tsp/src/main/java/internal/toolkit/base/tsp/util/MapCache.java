package internal.toolkit.base.tsp.util;

import jdplus.toolkit.base.tsp.util.ShortLivedCache;
import lombok.NonNull;

import java.util.Map;

public sealed interface MapCache<K, V>
        extends ShortLivedCache<K, V>
        permits FileMapCache, SimpleMapCache, TtlMapCache {

    @NonNull Map<K, ?> getMap();
}
