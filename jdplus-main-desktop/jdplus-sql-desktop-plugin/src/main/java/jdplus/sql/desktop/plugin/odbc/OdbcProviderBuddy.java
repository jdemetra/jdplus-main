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
package jdplus.sql.desktop.plugin.odbc;

import ec.util.completion.AutoCompletionSource;
import ec.util.completion.ExtAutoCompletionSource;
import internal.sql.base.api.DefaultConnectionSource;
import jdplus.sql.base.api.ConnectionManager;
import jdplus.sql.base.api.ConnectionSource;
import jdplus.sql.base.api.HasSqlProperties;
import jdplus.sql.base.api.odbc.OdbcBean;
import jdplus.sql.desktop.plugin.SqlColumnListCellRenderer;
import jdplus.sql.desktop.plugin.SqlProviderBuddy;
import jdplus.sql.desktop.plugin.SqlTableListCellRenderer;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.actions.Configurable;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceProviderBuddy;
import jdplus.toolkit.desktop.plugin.tsproviders.TsProviderProperties;
import jdplus.toolkit.desktop.plugin.util.SimpleHtmlCellRenderer;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcRegistry;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(DataSourceProviderBuddy.class)
public final class OdbcProviderBuddy implements DataSourceProviderBuddy, Configurable {

    private static final String SOURCE = "ODBCPRVDR";

    private final ConnectionManager manager;
    private final AutoCompletionSource dbSource;
    private final ListCellRenderer dbRenderer;
    private final ListCellRenderer tableRenderer;
    private final ListCellRenderer columnRenderer;

    public OdbcProviderBuddy() {
        this.manager = getOdbcConnectionManager();
        this.dbSource = odbcDsnSource();
        this.dbRenderer = new SimpleHtmlCellRenderer<>((OdbcDataSource o) -> "<html><b>" + o.getName() + "</b> - <i>" + o.getServerName() + "</i>");
        this.tableRenderer = new SqlTableListCellRenderer();
        this.columnRenderer = new SqlColumnListCellRenderer();
    }

    @Override
    public void configure() {
        launchOdbcDataSourceAdministrator();
    }

    @Override
    public String getProviderName() {
        return SOURCE;
    }

    @Override
    public Image getIconOrNull(int type, boolean opened) {
        return ImageUtilities.loadImage("jdplus/sql/desktop/plugin/database.png", true);
    }

    @Override
    public List<Sheet.Set> getSheetOfBeanOrNull(@NonNull Object bean) throws IntrospectionException {
        return bean instanceof OdbcBean ? createSheetSets((OdbcBean) bean) : null;
    }

    private List<Sheet.Set> createSheetSets(OdbcBean bean) {
        NodePropertySetBuilder b = new NodePropertySetBuilder();
        return Arrays.asList(
                createSource(b, bean),
                createCube(b, bean),
                createParsing(b, bean),
                createCache(b, bean)
        );
    }

    @NbBundle.Messages({
            "bean.source.display=Source",
            "bean.source.description=",
            "bean.dsn.display=Data source name",
            "bean.dsn.description=Data structure describing the connection to the database.",
            "bean.table.display=Table name",
            "bean.table.description=The name of the table (or view) that contains observations.",})
    private Sheet.Set createSource(NodePropertySetBuilder b, OdbcBean bean) {
        b.reset("source")
                .display(Bundle.bean_source_display())
                .description(Bundle.bean_source_description());

        b.withAutoCompletion()
                .select("dsn", bean::getDsn, bean::setDsn)
                .source(dbSource)
                .cellRenderer(dbRenderer)
                .display(Bundle.bean_dsn_display())
                .description(Bundle.bean_dsn_description())
                .add();

        b.withAutoCompletion()
                .select("table", bean::getTable, bean::setTable)
                .source(SqlProviderBuddy.getTableSource(manager, bean::getDsn, bean::getTable))
                .cellRenderer(tableRenderer)
                .display(Bundle.bean_table_display())
                .description(Bundle.bean_table_description())
                .add();

        return b.build();
    }

    @NbBundle.Messages({
            "bean.cube.display=Cube structure",
            "bean.cube.description=",})
    private Sheet.Set createCube(NodePropertySetBuilder b, OdbcBean bean) {
        b.reset("cube")
                .display(Bundle.bean_cube_display())
                .description(Bundle.bean_cube_description());

        TsProviderProperties.addTableAsCubeStructure(b, bean::getCube, bean::setCube,
                SqlProviderBuddy.getColumnSource(manager, bean::getDsn, bean::getTable),
                columnRenderer
        );

        return b.build();
    }

    @NbBundle.Messages({
            "bean.parsing.display=Parsing",
            "bean.parsing.description=",})
    private Sheet.Set createParsing(NodePropertySetBuilder b, OdbcBean bean) {
        b.reset("parsing")
                .display(Bundle.bean_parsing_display())
                .description(Bundle.bean_parsing_description());

        TsProviderProperties.addTableAsCubeParsing(b, bean::getCube, bean::setCube);

        return b.build();
    }

    @NbBundle.Messages({
            "bean.cache.display=Cache",
            "bean.cache.description=Mechanism used to improve performance.",})
    private Sheet.Set createCache(NodePropertySetBuilder b, OdbcBean bean) {
        b.reset("cache")
                .display(Bundle.bean_cache_display())
                .description(Bundle.bean_cache_description());

        TsProviderProperties.addBulkCube(b, bean::getCache, bean::setCache);

        return b.build();
    }

    private static void launchOdbcDataSourceAdministrator() {
        try {
            // %SystemRoot%\\system32\\odbcad32.exe
            Runtime.getRuntime().exec("odbcad32.exe");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static ConnectionManager getOdbcConnectionManager() {
        Optional<HasSqlProperties> provider = TsManager.get()
                .getProvider(SOURCE)
                .filter(HasSqlProperties.class::isInstance)
                .map(HasSqlProperties.class::cast);
        return provider.isPresent()
                ? provider.orElseThrow().getConnectionManager()
                : new FailingConnectionManager("Cannot load OdbcProvider");
    }

    private static final class FailingConnectionManager implements ConnectionManager {

        private final String cause;

        public FailingConnectionManager(String cause) {
            this.cause = cause;
        }

        @Override
        public @NonNull String getId() {
            return "failing";
        }

        @Override
        public @NonNull ConnectionSource getSource(@NonNull String dbName) {
            return new DefaultConnectionSource(o -> {
                throw new SQLException(cause);
            }, dbName);
        }
    }

    private static AutoCompletionSource odbcDsnSource() {
        return ExtAutoCompletionSource
                .builder(OdbcProviderBuddy::getDataSources)
                .behavior(AutoCompletionSource.Behavior.ASYNC)
                .postProcessor(OdbcProviderBuddy::getDataSources)
                .valueToString(OdbcDataSource::getName)
                .cache(new ConcurrentHashMap<>(), o -> "", AutoCompletionSource.Behavior.SYNC)
                .build();
    }

    private static List<OdbcDataSource> getDataSources() throws Exception {
        Optional<OdbcRegistry> odbcRegistry = OdbcRegistry.ofServiceLoader();
        return odbcRegistry.isPresent()
                ? odbcRegistry.orElseThrow().getDataSources(OdbcDataSource.Type.SYSTEM, OdbcDataSource.Type.USER)
                : Collections.emptyList();
    }

    private static List<OdbcDataSource> getDataSources(List<OdbcDataSource> allValues, String term) {
        Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
        return allValues.stream()
                .filter(o -> filter.test(o.getName()) || filter.test(o.getServerName()))
                .sorted(Comparator.comparing(OdbcDataSource::getName))
                .collect(Collectors.toList());
    }
}
