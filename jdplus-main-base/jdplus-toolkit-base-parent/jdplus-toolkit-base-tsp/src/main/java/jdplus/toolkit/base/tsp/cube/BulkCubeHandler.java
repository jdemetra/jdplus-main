package jdplus.toolkit.base.tsp.cube;

import jdplus.toolkit.base.tsp.util.PropertyHandler;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Function;

@lombok.Builder(toBuilder = true)
public final class BulkCubeHandler implements PropertyHandler<BulkCube> {

    @NonNull
    private final PropertyHandler<Duration> ttl;

    @NonNull
    private final PropertyHandler<Integer> depth;

    @Override
    public @NonNull BulkCube get(@NonNull Function<? super String, ? extends CharSequence> properties) {
        return BulkCube
                .builder()
                .ttl(ttl.get(properties))
                .depth(depth.get(properties))
                .build();
    }

    @Override
    public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable BulkCube value) {
        if (value != null) {
            ttl.set(properties, value.getTtl());
            depth.set(properties, value.getDepth());
        }
    }
}
