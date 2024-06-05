/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.desktop.plugin.util;

import ec.util.chart.swing.Charts;
import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.FileLoader;
import jdplus.toolkit.desktop.plugin.DemetraBehaviour;
import jdplus.toolkit.desktop.plugin.DemetraUI;
import jdplus.toolkit.desktop.plugin.Persistable;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.concurrent.ThreadPriority;
import jdplus.toolkit.desktop.plugin.concurrent.UIExecutors;
import jdplus.toolkit.desktop.plugin.core.star.StarStep;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceProviderBuddy;
import jdplus.toolkit.desktop.plugin.ui.mru.MruProvidersStep;
import jdplus.toolkit.desktop.plugin.ui.mru.MruWorkspacesStep;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import nbbrd.design.MightBePromoted;
import nbbrd.io.FileParser;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.xml.bind.Jaxb;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@lombok.extern.java.Log
public final class Installer extends ModuleInstall {

    public static final InstallerStep STEP = InstallerStep.all(new AppVersionStep(),
            new ProvidersV3Step(),
            new ProviderBuddyStep(),
            new TsVariableStep(),
            new MruProvidersStep(),
            new MruWorkspacesStep(),
            new JFreeChartStep(),
            new StarStep(),
            new DemetraOptionsStep(),
            new FileChooserStep()
    );

    @Override
    public void restored() {
        super.restored();
        STEP.restore();
    }

    @Override
    public void close() {
        try {
            STEP.prefs().flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        STEP.close();
        super.close();
    }

    @Override
    public boolean closing() {
        return WorkspaceFactory.getInstance().closeWorkspace(true);
    }

    private static final class AppVersionStep extends InstallerStep {

        @MightBePromoted
        private static Properties parseProperties(InputStream stream) throws IOException {
            Properties p = new Properties();
            p.load(stream);
            return p;
        }

        @Override
        public void restore() {
            try {
                Properties properties = FileParser.onParsingStream(AppVersionStep::parseProperties)
                        .parseResource(Installer.class, "/META-INF/maven/eu.europa.ec.joinup.sat/jdplus-toolkit-desktop-plugin/pom.properties");
                System.setProperty("netbeans.buildnumber", properties.getProperty("version"));
            } catch (IOException ex) {
                log.log(Level.WARNING, "Cannot load version", ex);
            }
        }
    }

    private static final class ProvidersV3Step extends InstallerStep.LookupStep<TsProvider> {

        final Preferences prefs = prefs();
        final Parser<File[]> pathsParser = Jaxb.Parser.of(PathsBean.class).asParser().andThen(o -> o.paths != null ? o.paths : new File[0]);
        final Formatter<File[]> pathsFormatter = Jaxb.Formatter.of(PathsBean.class).asFormatter().compose(PathsBean::create);

        ProvidersV3Step() {
            super(TsProvider.class);
        }

        private void register(Iterable<? extends TsProvider> providers) {
            Preferences pathsNode = prefs.node("paths");
            for (TsProvider o : providers) {
                TsManager.get().register(o);
                if (o instanceof FileLoader<?> fileLoader) {
                    tryGet(pathsNode, o.getSource(), pathsParser)
                            .ifPresent(fileLoader::setPaths);
                }
            }
//            TsManager.get().register(new PocProvider());
        }

        private void unregister(Iterable<? extends TsProvider> providers) {
            Preferences pathsNode = prefs.node("paths");
            for (TsProvider o : providers) {
                if (o instanceof FileLoader<?> fileLoader) {
                    tryPut(pathsNode, o.getSource(), pathsFormatter, fileLoader.getPaths());
                }
                TsManager.get().unregister(o);
            }
        }

        private static <X> List<X> except(List<X> l, List<X> r) {
            List<X> result = new ArrayList<>(l);
            result.removeAll(r);
            return result;
        }

        private static String toString(Stream<? extends TsProvider> providers) {
            return providers
                    .map(o -> o.getSource() + "(" + o.getClass().getName() + ")")
                    .collect(Collectors.joining(", "));
        }

        @Override
        protected void onResultChanged(Lookup.Result<TsProvider> lookup) {
            List<TsProvider> old = TsManager.get().getProviders().collect(Collectors.toList());
            List<TsProvider> current = new ArrayList<>(lookup.allInstances());

            unregister(except(old, current));
            register(except(current, old));
        }

        @Override
        protected void onRestore(Lookup.Result<TsProvider> lookup) {
            register(lookup.allInstances());
            log.log(Level.FINE, "Loaded providers: [{}]", toString(TsManager.get().getProviders()));
        }

        @Override
        protected void onClose(Lookup.Result<TsProvider> lookup) {
            unregister(TsManager.get().getProviders().collect(Collectors.toList()));
            try {
                prefs.flush();
            } catch (BackingStoreException ex) {
                log.log(Level.WARNING, "Can't flush storage", ex);
            }
            TsManager.get().close();
        }

        @XmlRootElement(name = "paths")
        static class PathsBean {

            @XmlElement(name = "path")
            public File[] paths;

            static PathsBean create(File[] o) {
                PathsBean result = new PathsBean();
                result.paths = o;
                return result;
            }
        }
    }

    private static final class TsVariableStep extends InstallerStep {

        @Override
        public void restore() {
//            TsVariable.register();
//            DynamicTsVariable.register();
        }
    }

    private static final class ProviderBuddyStep extends InstallerStep.LookupStep<DataSourceProviderBuddy> {

        final Preferences prefs = prefs();

        public ProviderBuddyStep() {
            super(DataSourceProviderBuddy.class);
        }

        @Override
        protected void onRestore(Lookup.Result<DataSourceProviderBuddy> lookup) {
            for (DataSourceProviderBuddy buddy : lookup.allInstances()) {
                if (buddy instanceof Persistable persistable) {
                    tryGet(prefs, buddy.getProviderName(), XmlConfig.xmlParser()).ifPresent(persistable::setConfig);
                }
            }
        }

        @Override
        protected void onResultChanged(Lookup.Result<DataSourceProviderBuddy> lookup) {
            onRestore(lookup);
        }

        @Override
        protected void onClose(Lookup.Result<DataSourceProviderBuddy> lookup) {
            for (DataSourceProviderBuddy buddy : lookup.allInstances()) {
                if (buddy instanceof Persistable persistable) {
                    tryPut(prefs, buddy.getProviderName(), XmlConfig.xmlFormatter(false), persistable.getConfig());
                }
            }
        }
    }

    private static final class JFreeChartStep extends InstallerStep {

        @Override
        public void restore() {
            ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
            BarRenderer.setDefaultBarPainter(new StandardBarPainter());
            log.log(Level.INFO, "ChartPanel buffer " + (Charts.USE_CHART_PANEL_BUFFER ? "enabled" : "disabled"));
        }
    }

    private static final class DemetraOptionsStep extends InstallerStep {

        private final Preferences options = prefs().node("options");

        private static final String UI = "ui", BEHAVIOUR = "behaviour";

        @Override
        public void restore() {
            load(options.node(UI), DemetraUI.get());
            load(options.node(BEHAVIOUR), DemetraBehaviour.get());
        }

        @Override
        public void close() {
            store(options.node(UI), DemetraUI.get());
            store(options.node(BEHAVIOUR), DemetraBehaviour.get());
        }
    }

    private static final class FileChooserStep extends InstallerStep {

        @Override
        public void restore() {
            UIExecutors.newSingleThreadExecutor(ThreadPriority.MIN)
                    .execute(FileChooserStep::warmupFileSystemView);
        }

        private static void warmupFileSystemView() {
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                List<SystemFile> files = SystemFile.load();
                log.log(Level.INFO, "FileSystemView warmed up on " + files.size() + " files in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
            } catch (RuntimeException ex) {
                log.log(Level.WARNING, "Failed to warmup FileSystemView", ex);
            }
        }

        private record SystemFile(File file, String displayName, Icon icon) {

            static List<SystemFile> load() {
                FileSystemView fsv = FileSystemView.getFileSystemView();
                Dimension shortcutsIconSize = getShortcutsIconSize();
                return Stream.concat(
                                Stream.of(fsv.getChooserComboBoxFiles()).map(f -> SystemFile.of(f, fsv, null)),
                                Stream.of(fsv.getChooserShortcutPanelFiles()).map(f -> SystemFile.of(f, fsv, shortcutsIconSize)))
                        .toList();
            }

            private static SystemFile of(File file, FileSystemView fsv, Dimension size) {
                return size == null
                        ? new SystemFile(file, fsv.getSystemDisplayName(file), fsv.getSystemIcon(file))
                        : new SystemFile(file, fsv.getSystemDisplayName(file), fsv.getSystemIcon(file, size.width, size.height));
            }

            private static Dimension getShortcutsIconSize() {
                Dimension result = UIManager.getDimension("FileChooser.shortcuts.iconSize");
                return result == null ? new Dimension(32, 32) : result;
            }
        }
    }
}
