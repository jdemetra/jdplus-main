package jdplus.toolkit.desktop.plugin;

import jdplus.main.desktop.design.GlobalService;
import jdplus.toolkit.desktop.plugin.util.CollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.Collections2;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import ec.util.chart.ColorScheme;
import ec.util.chart.impl.SmartColorScheme;
import ec.util.chart.swing.SwingColorSchemeSupport;
import nbbrd.design.swing.OnEDT;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@GlobalService
public final class ColorSchemeManager {

    @NonNull
    public static ColorSchemeManager get() {
        return LazyGlobalService.get(ColorSchemeManager.class, ColorSchemeManager::new);
    }

    private final CollectionSupplier<ColorScheme> providers = CollectionSupplier.ofLookup(ColorScheme.class);
    private final WeakHashMap<String, SwingColorSchemeSupport> cache = new WeakHashMap<>();

    private ColorSchemeManager() {
    }

    @NonNull
    public ColorScheme getMainColorScheme() {
        String mainColorSchemeName = DemetraUI.get().getColorSchemeName();
        return providers
                .stream()
                .filter(Collections2.compose(Predicate.isEqual(mainColorSchemeName), ColorScheme::getName))
                .map(ColorScheme.class::cast)
                .findFirst()
                .orElseGet(SmartColorScheme::new);
    }

    @NonNull
    public List<? extends ColorScheme> getColorSchemes() {
        return providers
                .stream()
                .sorted(Comparator.comparing(ColorScheme::getDisplayName))
                .collect(Collectors.toList());
    }

    @OnEDT
    @NonNull
    public SwingColorSchemeSupport getSupport(@Nullable ColorScheme colorScheme) {
        ColorScheme result = colorScheme != null ? colorScheme : getMainColorScheme();
        return cache.computeIfAbsent(result.getName(), name -> SwingColorSchemeSupport.from(result));
    }
}
