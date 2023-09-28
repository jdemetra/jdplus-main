package internal.toolkit.base.tsp.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static jdplus.toolkit.base.tsp.util.ShortLivedCachingTest.assertCompliance;
import static jdplus.toolkit.base.tsp.util.ShortLivedCachingTest.touch;

public class CaffeineCachingTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        var tickerInNanos = new AtomicLong(0);

        assertCompliance(
                new CaffeineCaching(tickerInNanos::get),
                duration -> tickerInNanos.addAndGet(duration.toNanos()),
                file -> touch(file, FileTime.from(tickerInNanos.addAndGet(Duration.ofMillis(1).toNanos()), TimeUnit.NANOSECONDS)),
                temp
        );
    }

}
