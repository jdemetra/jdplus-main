package jdplus.toolkit.desktop.plugin.completion;

import ec.util.completion.swing.JAutoCompletion;
import jdplus.main.desktop.design.GlobalService;
import jdplus.toolkit.desktop.plugin.util.CollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.FixmeCollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;

import javax.swing.text.JTextComponent;

import static jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend.*;

/**
 *
 */
@GlobalService
public final class AutoCompletionManager {

    @NonNull
    public static AutoCompletionManager get() {
        return LazyGlobalService.get(AutoCompletionManager.class, AutoCompletionManager::new);
    }

    private AutoCompletionManager() {
    }

    private final CollectionSupplier<AutoCompletionSpi> providers = FixmeCollectionSupplier.of(AutoCompletionSpi.class, buildServiceLoader()::get);

    @MightBeGenerated
    private static AutoCompletionSpiLoader buildServiceLoader() {
        return AutoCompletionSpiLoader.builder().backend(lookupFactory(), lookupStreamer(), lookupReloader()).build();
    }

    @NonNull
    public JAutoCompletion bind(@NonNull Class<?> path, @NonNull JTextComponent textComponent) {
        return bind(path.getName(), textComponent);
    }

    @NonNull
    public JAutoCompletion bind(@NonNull String path, @NonNull JTextComponent textComponent) {
        return providers
                .stream()
                .filter(spi -> spi.getPath().equals(path))
                .findFirst()
                .map(spi -> spi.bind(textComponent))
                .orElseGet(() -> new JAutoCompletion(textComponent));
    }
}
