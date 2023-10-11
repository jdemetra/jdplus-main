/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core.star;

import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.star.StarListManager;
import jdplus.toolkit.desktop.plugin.util.InstallerStep;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.openide.util.NbPreferences;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Comparator.reverseOrder;
import static jdplus.toolkit.base.api.util.Collections2.streamOf;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public class StarStep extends InstallerStep {

    @Override
    public void restore() {
        TsManager tsManager = TsManager.get();
        StarListManager stars = StarListManager.get();
        loadSources(getStarNode())
                .forEach(dataSource -> {
                    stars.add(dataSource);
                    tsManager.open(dataSource);
                });
    }

    @Override
    public void close() {
        StarListManager stars = StarListManager.get();
        storeSources(getStarNode(), stars);
    }

    private static Preferences getStarNode() {
        return NbPreferences.forModule(StarStep.class).node("Star");
    }

    private static final String DATASOURCE_PROPERTY = "StarDataSource";

    @VisibleForTesting
    static Iterable<DataSource> loadSources(Preferences prefs) {
        try {
            return Stream.of(prefs.childrenNames())
                    .map(pathName -> prefs.node(pathName).get(DATASOURCE_PROPERTY, null))
                    .map(Parser.of(DataSource::parse)::parse)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (BackingStoreException ex) {
            log.log(Level.WARNING, "Can't load stared data sources", ex);
            return emptyList();
        }
    }

    @VisibleForTesting
    static void storeSources(Preferences prefs, Iterable<DataSource> dataSources) {
        try {
            for (String i : prefs.childrenNames()) {
                prefs.node(i).removeNode();
            }
            Supplier<String> pathName = getPathNameSupplier();
            streamOf(dataSources)
                    .map(Formatter.of(DataSource::toString)::formatAsString)
                    .filter(Objects::nonNull)
                    .forEach(o -> prefs.node(pathName.get()).put(DATASOURCE_PROPERTY, o));
            prefs.flush();
        } catch (BackingStoreException ex) {
            log.log(Level.WARNING, "Can't store stared data sources", ex);
        }
    }

    @VisibleForTesting
    static Supplier<String> getPathNameSupplier() {
        AtomicInteger index = new AtomicInteger();
        return () -> String.valueOf(index.getAndIncrement());
    }
}
