package jdplus.toolkit.desktop.plugin.components.parts;

import ec.util.list.swing.JLists;
import jdplus.toolkit.base.api.timeseries.*;
import jdplus.toolkit.desktop.plugin.beans.PropertyChangeSource;
import jdplus.toolkit.desktop.plugin.components.TsSelectionBridge;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.List;

import static java.util.List.of;
import static java.util.stream.Collectors.joining;
import static jdplus.toolkit.base.api.timeseries.TsCollection.toTsCollection;
import static org.assertj.core.api.Assertions.assertThat;

class HasTsCollectionSupportTest {

    @Test
    void testSplitMenu() {
        TsData invalid = TsData.ofInternal(TsPeriod.monthly(2010, 1), new double[]{1, 2});
        TsData valid0 = TsData.ofInternal(TsPeriod.yearly(2010), new double[]{1, 2});
        TsData valid1 = TsData.ofInternal(TsPeriod.yearly(2011), new double[]{1, 2});

        MockedTsCollectionComponent c = new MockedTsCollectionComponent();
        assertThat(HasTsCollectionSupport.newSplitMenu(c))
                .isNot(enabledOn(c, of(), of()).as("enabled on no data & no selection"))
                .isNot(enabledOn(c, of(invalid), of()).as("enabled on single invalid & no selection"))
                .isNot(enabledOn(c, of(invalid), of(0)).as("enabled on single invalid & single selection"))
                .is(enabledOn(c, of(valid0), of()).as("enabled on single valid & no selection"))
                .is(enabledOn(c, of(valid0), of(0)).as("enabled on single valid & single selection"))
                .isNot(enabledOn(c, of(valid0, valid1), of()).as("enabled on multiple valid & no selection"))
                .is(enabledOn(c, of(valid0, valid1), of(0)).as("enabled on multiple valid & first selection"))
                .is(enabledOn(c, of(valid0, valid1), of(1)).as("enabled on multiple valid & last selection"))
                .isNot(enabledOn(c, of(valid0, valid1), of(0, 1)).as("enabled on multiple valid & multiple selection"))
                .isNot(enabledOn(c, of(invalid, valid1), of(0)).as("enabled on invalid+valid & invalid selection"))
                .is(enabledOn(c, of(invalid, valid1), of(1)).as("enabled on invalid+valid & valid selection"))
        ;
    }

    private static final class MockedTsCollectionComponent extends JComponent implements HasTsCollection, PropertyChangeSource {

        @lombok.experimental.Delegate
        private final HasTsCollection col;

        private final TsSelectionBridge tsSelectionBridge;

        MockedTsCollectionComponent() {
            this.col = HasTsCollectionSupport.of(this::firePropertyChange, TsInformationType.All);
            HasTsCollectionSupport.registerActions(this, this.getActionMap());
            this.tsSelectionBridge = new TsSelectionBridge(this::firePropertyChange);
            tsSelectionBridge.register(this);
        }

        void applyData(List<TsData> items) {
            setTsCollection(items.stream().map(Ts::of).collect(toTsCollection()));
        }

        void applySelection(List<Integer> selection) {
            JLists.setSelectionIndexStream(getTsSelectionModel(), selection.stream().mapToInt(Integer::intValue));
        }
    }

    private static Condition<JMenuItem> enabledOn(MockedTsCollectionComponent c, List<TsData> items, List<Integer> selection) {
        return new Condition<>(value -> {
            c.applyData(items);
            c.applySelection(selection);
            return value.isEnabled();
        }, "enabled on [%s] and %s", items.stream().map(TsData::getDomain).map(TsDomain::toShortString).collect(joining(", ")), selection);
    }
}