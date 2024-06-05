package jdplus.x13.desktop.plugin.x13.ui;

import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.Persistable;
import jdplus.toolkit.desktop.plugin.beans.PropertyChangeSource;
import jdplus.main.desktop.design.GlobalService;
import jdplus.main.desktop.design.SwingProperty;
import jdplus.sa.desktop.plugin.output.OutputSelection;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import jdplus.toolkit.desktop.plugin.util.Persistence;
import jdplus.x13.desktop.plugin.x13.diagnostics.X13DiagnosticsFactoryBuddy;
import jdplus.sa.base.api.SaDiagnosticsFactory;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import lombok.NonNull;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import jdplus.x13.base.core.x13.X13Factory;
import jdplus.x13.base.core.x13.X13Results;
import nbbrd.design.MightBeGenerated;
import org.openide.util.Lookup;

@GlobalService
public final class X13UI implements PropertyChangeSource.WithWeakListeners, Persistable {

    public static void setDiagnostics() {
        Lookup.getDefault().lookupAll(X13DiagnosticsFactoryBuddy.class).stream().forEach(X13DiagnosticsFactoryBuddy::commit);
        // updates the diagnostics factories of the main processor
        Stream<SaDiagnosticsFactory<?, X13Results>> map = Lookup.getDefault()
                .lookupAll(X13DiagnosticsFactoryBuddy.class)
                .stream()
                .map(buddy -> (SaDiagnosticsFactory<?, X13Results>) buddy.createFactory());
        List<SaDiagnosticsFactory<?, X13Results>> factories = new ArrayList();
        map.forEach(fac -> factories.add(fac));
        X13Factory.getInstance().resetDiagnosticFactories(factories);
    }

    @NonNull
    public static X13UI get() {
        return LazyGlobalService.get(X13UI.class, X13UI::new);
    }

    private X13UI() {
    }

    @lombok.experimental.Delegate(types = PropertyChangeSource.class)
    private final PropertyChangeSupport broadcaster = new PropertyChangeSupport(this);

    private final List<String> selectedComponents = new ArrayList<>();
    private final List<String> selectedDiagnostics = new ArrayList<>();
    private int defaultSeriesParameter;

    @SwingProperty
    public static final String DEFAULT_SERIES_PARAMETER_PROPERTY = "defaultSeriesParameter";
    private static final int DEFAULT_SERIES_PARAMETER = -2;

    public int getDefaultSeriesParameter() {
        return defaultSeriesParameter;
    }

    public void setDefaultSeriesParameter(int value) {
        defaultSeriesParameter = value;
    }

    public void setSelectedDiagnostics(List<String> diags) {
        selectedDiagnostics.clear();
        selectedDiagnostics.addAll(diags);
    }

    public List<String> getSelectedDiagnostics() {
        return Collections.unmodifiableList(selectedDiagnostics);
    }

    public List<String> allDiagnostics() {
        return OutputSelection.matrixItems(Collections.singletonList(X13Factory.getInstance()));
    }

    public void setSelectedComponents(List<String> cmps) {
        selectedComponents.clear();
        selectedComponents.addAll(cmps);
    }

    public List<String> getSelectedComponents() {
        return Collections.unmodifiableList(selectedComponents);
    }

    public List<String> allComponents() {
        return OutputSelection.seriesItems(Collections.singletonList(X13Factory.getInstance()));
    }

    @Override
    public @NonNull Config getConfig() {
        return PERSISTENCE.loadConfig(this);
    }

    @Override
    public void setConfig(@NonNull Config config) {
        PERSISTENCE.storeConfig(this, config);
    }

    private static final String COMPONENTS = "series", DIAGS = "matrix";

    @MightBeGenerated
    private static final Persistence<X13UI> PERSISTENCE = Persistence
            .builderOf(X13UI.class)
            .name("demetra-x13")
            .version("3.0.0")
            .with(
                    PropertyHandler.onStringList(COMPONENTS, Collections.emptyList(), ','),
                    X13UI::getSelectedComponents,
                    X13UI::setSelectedComponents
            )
            .with(
                    PropertyHandler.onStringList(DIAGS, Collections.emptyList(), ','),
                    X13UI::getSelectedDiagnostics,
                    X13UI::setSelectedDiagnostics
            )
            .with(
                    PropertyHandler.onInteger(DEFAULT_SERIES_PARAMETER_PROPERTY, DEFAULT_SERIES_PARAMETER),
                    X13UI::getDefaultSeriesParameter,
                    X13UI::setDefaultSeriesParameter
            )
            .build();
}
