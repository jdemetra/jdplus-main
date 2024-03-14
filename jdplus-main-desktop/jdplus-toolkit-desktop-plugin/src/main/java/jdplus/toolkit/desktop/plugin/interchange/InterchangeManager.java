package jdplus.toolkit.desktop.plugin.interchange;

import internal.uihelpers.FixmeCollectionSupplier;
import jdplus.main.desktop.design.GlobalService;
import jdplus.toolkit.desktop.plugin.util.CollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import org.openide.util.Exceptions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@GlobalService
public final class InterchangeManager {

    @NonNull
    public static InterchangeManager get() {
        return LazyGlobalService.get(InterchangeManager.class, InterchangeManager::new);
    }

    private final CollectionSupplier<InterchangeSpi> providers;

    private InterchangeManager() {
        this(FixmeCollectionSupplier.of(InterchangeSpi.class, InterchangeSpiLoader::load));
    }

    @VisibleForTesting
    InterchangeManager(CollectionSupplier<InterchangeSpi> providers) {
        this.providers = providers;
    }

    @NonNull
    public Collection<? extends InterchangeSpi> all() {
        return providers.get();
    }

    @NonNull
    public JMenuItem newImportMenu(@NonNull List<? extends Importable> importables) {
        JMenu result = new JMenu();
        result.setText("Import from");
        providers.get().forEach(o -> result.add(newImportMenu(o, importables)));
        return result;
    }

    private JMenuItem newImportMenu(InterchangeSpi o, List<? extends Importable> importables) {
        JMenuItem result = new JMenuItem(new Import(o, importables));
        result.setText(o.getDisplayName());
        result.setEnabled(o.canImport(importables));
        return result;
    }

    @NonNull
    public JMenuItem newExportMenu(@NonNull List<? extends Exportable> exportables) {
        JMenu result = new JMenu();
        result.setText("Export to");
        providers.get().forEach(o -> result.add(newExportMenu(o, exportables)));
        return result;
    }

    private JMenuItem newExportMenu(InterchangeSpi o, List<? extends Exportable> exportables) {
        JMenuItem result = new JMenuItem(new Export(o, exportables));
        result.setText(o.getDisplayName());
        result.setEnabled(o.canExport(exportables));
        return result;
    }

    private static final class Import extends AbstractAction {

        private final InterchangeSpi o;
        private final List<? extends Importable> importables;

        public Import(InterchangeSpi o, List<? extends Importable> importables) {
            super(o.getName());
            this.o = o;
            this.importables = importables;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                o.performImport(importables);
            } catch (IOException | IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static final class Export extends AbstractAction {

        private final InterchangeSpi o;
        private final List<? extends Exportable> exportables;

        public Export(InterchangeSpi o, List<? extends Exportable> exportables) {
            super(o.getName());
            this.o = o;
            this.exportables = exportables;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                o.performExport(exportables);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
