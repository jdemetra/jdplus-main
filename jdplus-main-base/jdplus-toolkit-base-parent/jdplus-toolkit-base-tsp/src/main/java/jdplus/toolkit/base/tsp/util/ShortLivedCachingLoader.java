package jdplus.toolkit.base.tsp.util;

@Deprecated
public final class ShortLivedCachingLoader {

    private static final ShortLivedCaching RESOURCE = internal.toolkit.base.tsp.util.ShortLivedCachingLoader.load();

    private ShortLivedCachingLoader() {
    }

    /**
     * Gets a {@link jdplus.toolkit.base.tsp.util.ShortLivedCaching} instance.
     * <br>This method is thread-safe.
     * @return the current non-null value
     */
    public static ShortLivedCaching get() {
        return RESOURCE;
    }
}
