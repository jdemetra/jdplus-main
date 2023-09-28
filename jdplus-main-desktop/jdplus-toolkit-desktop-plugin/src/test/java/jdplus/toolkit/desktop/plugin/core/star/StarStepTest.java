package jdplus.toolkit.desktop.plugin.core.star;

import jdplus.toolkit.base.tsp.DataSource;
import nbbrd.design.MightBePromoted;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class StarStepTest {

    @Test
    public void testGetPathNameSupplier() {
        Supplier<String> pathNameSupplier = StarStep.getPathNameSupplier();
        assertThat(pathNameSupplier.get()).isEqualTo("0");
        assertThat(pathNameSupplier.get()).isEqualTo("1");
        assertThat(pathNameSupplier.get()).isEqualTo("2");
    }

    @Test
    public void testLoadStore() {
        Preferences prefs = new MockedPreferences(null, "");

        assertThat(StarStep.loadSources(prefs))
                .isEmpty();

        List<DataSource> dataSources = List.of(DataSource.of("p1", "v1"), DataSource.of("p2", "v1"));

        StarStep.storeSources(prefs, dataSources);
        assertThat(StarStep.loadSources(prefs))
                .containsExactlyElementsOf(dataSources);

        StarStep.storeSources(prefs, emptyList());
        assertThat(StarStep.loadSources(prefs))
                .isEmpty();
    }

    @MightBePromoted
    private static final class MockedPreferences extends AbstractPreferences {

        private final Map<String, String> data = new HashMap<>();
        private final Map<String, MockedPreferences> children = new HashMap<>();

        public MockedPreferences(MockedPreferences parent, String name) {
            super(parent, name);
        }

        @Override
        protected void putSpi(String key, String value) {
            data.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            return data.get(key);
        }

        @Override
        protected void removeSpi(String key) {
            data.remove(key);
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            Preferences parent = parent();
            if (parent instanceof MockedPreferences) {
                ((MockedPreferences) parent).children.remove(name());
            }
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            return data.keySet().toArray(String[]::new);
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            return children.keySet().toArray(String[]::new);
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            return children.computeIfAbsent(name, x -> new MockedPreferences(this, x));
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
        }
    }
}
