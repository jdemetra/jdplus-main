package jdplus.toolkit.desktop.plugin.completion;

import ec.util.completion.swing.JAutoCompletion;
import internal.uihelpers.FixmeCollectionSupplier;
import jdplus.main.desktop.design.GlobalService;
import jdplus.toolkit.desktop.plugin.util.CollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import lombok.NonNull;

import javax.swing.text.JTextComponent;

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

    private final CollectionSupplier<AutoCompletionSpi> providers = FixmeCollectionSupplier.of(AutoCompletionSpi.class, AutoCompletionSpiLoader::load);

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
