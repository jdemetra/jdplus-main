package jdplus.toolkit.base.tsp.util;

import lombok.NonNull;
import org.assertj.core.api.Condition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class ShortLivedCachingTest {

    @SuppressWarnings({"DataFlowIssue"})
    public static void assertCompliance(
            @NonNull ShortLivedCaching factory,
            @NonNull Consumer<Duration> timeIncrementer,
            @NonNull Consumer<File> fileIncrementer,
            @NonNull Path temp
    ) throws IOException {

        assertThatNullPointerException().isThrownBy(() -> factory.ofTtl(null));
        assertThatNullPointerException().isThrownBy(() -> factory.ofFile(null));

        assertTtl(factory, timeIncrementer);
        assertFile(factory, fileIncrementer, temp);
    }

    @SuppressWarnings({"DataFlowIssue"})
    private static void assertTtl(ShortLivedCaching factory, Consumer<Duration> timeIncrementer) {
        Duration ttl = Duration.ofMillis(10);
        ShortLivedCache<String, Integer> cache = factory.ofTtl(ttl);

        assertThatNullPointerException().isThrownBy(() -> cache.get(null));
        assertThatNullPointerException().isThrownBy(() -> cache.put(null, 1));
        assertThatNullPointerException().isThrownBy(() -> cache.put("k1", null));

        assertThat(cache.get("k1")).isNull();
        assertThat(cache.get("k2")).isNull();

        cache.put("k1", 1);
        assertThat(cache.get("k1")).is(nullOrEqualTo(1));
        assertThat(cache.get("k2")).isNull();

        cache.put("k1", 11);
        assertThat(cache.get("k1")).is(nullOrEqualTo(11));
        assertThat(cache.get("k2")).isNull();

        cache.put("k2", 2);
        assertThat(cache.get("k1")).is(nullOrEqualTo(11));
        assertThat(cache.get("k2")).is(nullOrEqualTo(2));

        timeIncrementer.accept(ttl.dividedBy(2));
        cache.put("k2", 22);
        timeIncrementer.accept(ttl.dividedBy(2));

        assertThat(cache.get("k1")).isNull();
        assertThat(cache.get("k2")).is(nullOrEqualTo(22));
    }

    @SuppressWarnings({"DataFlowIssue"})
    private static void assertFile(ShortLivedCaching factory, Consumer<File> touch, Path temp) {
        File file = create(temp);
        touch.accept(file);

        ShortLivedCache<String, Integer> cache = factory.ofFile(file);

        assertThatNullPointerException().isThrownBy(() -> cache.get(null));
        assertThatNullPointerException().isThrownBy(() -> cache.put(null, 1));
        assertThatNullPointerException().isThrownBy(() -> cache.put("k1", null));

        assertThat(cache.get("k1")).isNull();
        assertThat(cache.get("k2")).isNull();

        cache.put("k1", 1);
        assertThat(cache.get("k1")).is(nullOrEqualTo(1));
        assertThat(cache.get("k2")).isNull();

        cache.put("k1", 11);
        assertThat(cache.get("k1")).is(nullOrEqualTo(11));
        assertThat(cache.get("k2")).isNull();

        cache.put("k2", 2);
        assertThat(cache.get("k1")).is(nullOrEqualTo(11));
        assertThat(cache.get("k2")).is(nullOrEqualTo(2));

        touch.accept(file);
        cache.put("k2", 22);
        assertThat(cache.get("k1")).isNull();
        assertThat(cache.get("k2")).is(nullOrEqualTo(22));

        touch.accept(file);
        assertThat(cache.get("k1")).isNull();
        assertThat(cache.get("k2")).isNull();

        delete(file);
        cache.put("k2", 222);
        assertThat(cache.get("k2")).isNull();
    }

    public static <V> Condition<V> nullOrEqualTo(V value) {
        return new Condition<>((V found) -> found == null || found.equals(value), "null or equal to " + value);
    }

    public static File create(Path temp) {
        try {
            return Files.createTempFile(temp, "test", ".tmp").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void delete(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void touch(File file, FileTime fileTime) {
        try {
            Files.setLastModifiedTime(file.toPath(), fileTime);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
