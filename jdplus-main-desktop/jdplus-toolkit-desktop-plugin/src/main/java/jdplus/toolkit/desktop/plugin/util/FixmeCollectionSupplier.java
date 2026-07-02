package jdplus.toolkit.desktop.plugin.util;

import nbbrd.design.ThreadSafe;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import java.util.Collection;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Fix design flaw in java-service-util loaders.
 * Will be replaced by a future update of this library.
 * DO NOT USE
 *
 * @param <X>
 */
@ThreadSafe
@lombok.RequiredArgsConstructor
@lombok.extern.java.Log
public final class FixmeCollectionSupplier<X> implements CollectionSupplier<X>, LookupListener {

    public static <X> FixmeCollectionSupplier<X> of(Class<X> type, CollectionSupplier<X> delegate) {
        Lookup.Result<X> lookupResult = Lookup.getDefault().lookupResult(type);
        FixmeCollectionSupplier<X> result = new FixmeCollectionSupplier<>(type, lookupResult, delegate);
        lookupResult.addLookupListener(result);
        return result;
    }

    private final Class<X> type;

    private final Lookup.Result<X> lookupResult;

    private final CollectionSupplier<X> delegate;

    private Collection<? extends X> cache = null;

    @Override
    public synchronized Collection<? extends X> get() {
        return (cache == null) ? (cache = load()) : cache;
    }

    private Collection<? extends X> load() {
        log.log(Level.INFO, "Loading lookup cache for {0}", type.getSimpleName());
        Collection<? extends X> result = delegate.get();
        log.log(Level.INFO, "Loaded {0}", result.stream().map(o -> o.getClass().getName()).collect(Collectors.joining(", ")));
        return result;
    }

    @Override
    public synchronized void resultChanged(LookupEvent ev) {
        log.log(Level.INFO, "Invalidating lookup cache for {0}", type.getSimpleName());
        cache = null;
    }
}
