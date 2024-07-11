/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package jdplus.sql.desktop.plugin.jdbc;

import ec.util.completion.AutoCompletionSource;
import ec.util.completion.ExtAutoCompletionSource;
import ec.util.various.swing.FontAwesome;
import internal.sql.base.api.DefaultConnectionSource;
import internal.sql.desktop.plugin.DbIcon;
import jdplus.sql.base.api.ConnectionSource;
import jdplus.sql.base.api.jdbc.JdbcBean;
import jdplus.sql.base.api.jdbc.JdbcProvider;
import jdplus.sql.desktop.plugin.SqlColumnListCellRenderer;
import jdplus.sql.desktop.plugin.SqlProviderBuddy;
import jdplus.sql.desktop.plugin.SqlTableListCellRenderer;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.actions.Configurable;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceProviderBuddy;
import jdplus.toolkit.desktop.plugin.tsproviders.TsProviderProperties;
import jdplus.toolkit.desktop.plugin.util.SimpleHtmlCellRenderer;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import nbbrd.sql.jdbc.SqlConnectionSupplier;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ec.util.chart.impl.TangoColorScheme.DARK_ORANGE;
import static ec.util.chart.impl.TangoColorScheme.DARK_SCARLET_RED;
import static ec.util.chart.swing.SwingColorSchemeSupport.rgbToColor;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(DataSourceProviderBuddy.class)
public final class JdbcProviderBuddy implements DataSourceProviderBuddy, Configurable {

    private static final String SOURCE = "JNDI-JDBC";

    private final jdplus.sql.base.api.ConnectionManager manager;
    private final AutoCompletionSource dbSource;
    private final ListCellRenderer dbRenderer;
    private final ListCellRenderer tableRenderer;
    private final ListCellRenderer columnRenderer;
    private final Image warningBadge;
    private final Image errorBadge;

    public JdbcProviderBuddy() {
        this.manager = new DbExplorerConnectionManager();
        this.dbSource = dbExplorerSource();
        this.dbRenderer = new SimpleHtmlCellRenderer<>((DatabaseConnection o) -> "<html><b>" + o.getDisplayName() + "</b> - <i>" + o.getName() + "</i>");
        this.tableRenderer = new SqlTableListCellRenderer();
        this.columnRenderer = new SqlColumnListCellRenderer();
        this.warningBadge = FontAwesome.FA_EXCLAMATION_TRIANGLE.getImage(rgbToColor(DARK_ORANGE), 8f);
        this.errorBadge = FontAwesome.FA_EXCLAMATION_CIRCLE.getImage(rgbToColor(DARK_SCARLET_RED), 8f);
        overrideDefaultConnectionSupplier();
    }

    private Optional<JdbcProvider> getProvider() {
        return TsManager.get().getProvider(JdbcProvider.class, SOURCE);
    }

    // this overrides default connection supplier since we don't have JNDI in JavaSE
    private void overrideDefaultConnectionSupplier() {
        getProvider()
                .ifPresent(o -> o.setConnectionManager(manager));
    }

    @Override
    public String getProviderName() {
        return SOURCE;
    }

    @Override
    public Image getIconOrNull(int type, boolean opened) {
        return DbIcon.DATABASE.getImageIcon().getImage();
    }

    @Override
    public Image getIconOrNull(DataSource dataSource, int type, boolean opened) {
        Image image = getIconOrNull(type, opened);
        switch (getStatus(dataSource)) {
            case DISCONNECTED:
                return ImageUtilities.mergeImages(image, warningBadge, 8, 8);
            case MISSING:
                return ImageUtilities.mergeImages(image, errorBadge, 8, 8);
            default:
                return image;
        }
    }

    private DbConnStatus getStatus(DataSource dataSource) {
        return getProvider()
                .map(provider -> provider.decodeBean(dataSource))
                .map(bean -> DbConnStatus.lookupByDisplayName(bean.getDatabase()))
                .orElse(DbConnStatus.CONNECTED);
    }

    @Override
    public List<Sheet.Set> getSheetOrNull(@NonNull DataSource dataSource) {
        List<Sheet.Set> result = new ArrayList<>();
        NodePropertySetBuilder b = new NodePropertySetBuilder().name("Connection");

        getProvider()
                .map(provider -> provider.decodeBean(dataSource))
                .map(bean -> new DbConnStatusProperty(bean.getDatabase()))
                .ifPresent(b::add);

        result.add(b.build());
        return result;
    }

    @Override
    public List<Sheet.Set> getSheetOfBeanOrNull(@NonNull Object bean) throws IntrospectionException {
        return bean instanceof JdbcBean ? createSheetSets((JdbcBean) bean) : null;
    }

    private List<Sheet.Set> createSheetSets(JdbcBean bean) {
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
            "bean.database.display=Data source name",
            "bean.database.description=Data structure describing the connection to the database.",
            "bean.table.display=Table name",
            "bean.table.description=The name of the table (or view) that contains observations.",})
    private Sheet.Set createSource(NodePropertySetBuilder b, JdbcBean bean) {
        b.reset("source")
                .display(Bundle.bean_source_display())
                .description(Bundle.bean_source_description());

        b.withAutoCompletion()
                .select("database", bean::getDatabase, bean::setDatabase)
                .source(dbSource)
                .cellRenderer(dbRenderer)
                .display(Bundle.bean_database_display())
                .description(Bundle.bean_database_description())
                .add();

        b.withAutoCompletion()
                .select("table", bean::getTable, bean::setTable)
                .source(SqlProviderBuddy.getTableSource(manager, bean::getDatabase, bean::getTable))
                .cellRenderer(tableRenderer)
                .display(Bundle.bean_table_display())
                .description(Bundle.bean_table_description())
                .add();

        return b.build();
    }

    @NbBundle.Messages({
            "bean.cube.display=Cube structure",
            "bean.cube.description=",})
    private Sheet.Set createCube(NodePropertySetBuilder b, JdbcBean bean) {
        b.reset("cube")
                .display(Bundle.bean_cube_display())
                .description(Bundle.bean_cube_description());

        TsProviderProperties.addTableAsCubeStructure(b, bean::getCube, bean::setCube,
                SqlProviderBuddy.getColumnSource(manager, bean::getDatabase, bean::getTable),
                columnRenderer
        );

        return b.build();
    }

    @NbBundle.Messages({
            "bean.parsing.display=Parsing",
            "bean.parsing.description=",})
    private Sheet.Set createParsing(NodePropertySetBuilder b, JdbcBean bean) {
        b.reset("parsing")
                .display(Bundle.bean_parsing_display())
                .description(Bundle.bean_parsing_description());

        TsProviderProperties.addTableAsCubeParsing(b, bean::getCube, bean::setCube);

        return b.build();
    }

    @NbBundle.Messages({
            "bean.cache.display=Cache",
            "bean.cache.description=Mechanism used to improve performance.",})
    private Sheet.Set createCache(NodePropertySetBuilder b, JdbcBean bean) {
        b.reset("cache")
                .display(Bundle.bean_cache_display())
                .description(Bundle.bean_cache_description());

        TsProviderProperties.addBulkCube(b, bean::getCache, bean::setCache);

        return b.build();
    }

    @Override
    public void configure() {
        openServiceTab();
    }

    private static void openServiceTab() {
        Action serviceTabAction = FileUtil.getConfigObject("Actions/Window/org-netbeans-core-ide-ServicesTabAction.instance", Action.class);
        if (serviceTabAction != null) {
            EventQueue.invokeLater(() -> serviceTabAction.actionPerformed(null));
        }
    }

    private enum DbConnStatus {

        CONNECTED,
        DISCONNECTED,
        MISSING;

        public static DbConnStatus lookupByDisplayName(String dbName) {
            return DbExplorerUtil.getConnectionByDisplayName(dbName)
                    .map(o -> DbExplorerUtil.isConnected(o) ? CONNECTED : DISCONNECTED)
                    .orElse(MISSING);
        }
    }

    private static AutoCompletionSource dbExplorerSource() {
        return ExtAutoCompletionSource
                .builder(JdbcProviderBuddy::getDatabaseConnections)
                .behavior(AutoCompletionSource.Behavior.ASYNC)
                .postProcessor(JdbcProviderBuddy::getDatabaseConnections)
                .valueToString(DatabaseConnection::getDisplayName)
                .build();
    }

    private static List<DatabaseConnection> getDatabaseConnections() {
        return Arrays.asList(ConnectionManager.getDefault().getConnections());
    }

    private static List<DatabaseConnection> getDatabaseConnections(List<DatabaseConnection> values, String term) {
        Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
        return values.stream()
                .filter(o -> filter.test(o.getName()) || filter.test(o.getDisplayName()))
                .sorted(Comparator.comparing(DatabaseConnection::getDisplayName))
                .collect(Collectors.toList());
    }

    private static final class DbExplorerConnectionManager implements jdplus.sql.base.api.ConnectionManager {

        private final SqlConnectionSupplier supplier = new DbExplorerConnectionSupplier();

        @Override
        public @NonNull String getId() {
            return "dbexplorer";
        }

        @Override
        public @NonNull ConnectionSource getSource(@NonNull String connectionString) {
            return new DefaultConnectionSource(supplier, connectionString);
        }
    }

    @NbBundle.Messages({
            "# {0} - dbName",
            "dbexplorer.missingConnection=Cannot find connection named ''{0}''",
            "# {0} - dbName",
            "dbexplorer.noConnection=Not connected to the database ''{0}''",
            "# {0} - dbName",
            "dbexplorer.failedConnection=Failed to connect to the database ''{0}''",
            "# {0} - dbName",
            "dbexplorer.requestConnection=A process request a connection to the database ''{0}''.\nDo you want to open the connection dialog?",
            "dbexplorer.skip=Don't ask again"
    })
    private static final class DbExplorerConnectionSupplier implements SqlConnectionSupplier {

        private final Properties map = new Properties();

        @Override
        public Connection getConnection(String dbName) throws SQLException {
            DatabaseConnection o = DbExplorerUtil.getConnectionByDisplayName(dbName)
                    .orElseThrow(() -> new SQLException(Bundle.dbexplorer_missingConnection(dbName)));
            return getJDBCConnection(o);
        }

        private FailSafeConnection getJDBCConnection(DatabaseConnection o) throws SQLException {
            Connection conn = o.getJDBCConnection();
            if (conn != null) {
                return new FailSafeConnection(conn, o.getSchema());
            }
            if (connect(o) || connectWithDialog(o)) {
                return new FailSafeConnection(o.getJDBCConnection(), o.getSchema());
            }
            throw new SQLException(Bundle.dbexplorer_noConnection(o.getDisplayName()));
        }

        private boolean connect(DatabaseConnection o) throws SQLException {
            // let's try to connect without opening any dialog
            // must be done outside EDT
            if (!SwingUtilities.isEventDispatchThread()) {
                try {
                    // currently, the manager returns false if user is empty
                    // this behavior prevents some connections but it might be fixed in further versions
                    return ConnectionManager.getDefault().connect(o);
                } catch (DatabaseException ex) {
                    throw new SQLException(Bundle.dbexplorer_failedConnection(o.getDisplayName()), ex);
                }
            }
            return false;
        }

        private boolean connectWithDialog(final DatabaseConnection o) {
            try {
                return execInEDT(() -> {
                    String dbName = o.getDisplayName();
                    if (!isSkip(dbName)) {
                        JCheckBox checkBox = new JCheckBox(Bundle.dbexplorer_skip(), false);
                        Object[] msg = {Bundle.dbexplorer_requestConnection(dbName), checkBox};
                        Object option = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION));
                        setSkip(dbName, checkBox.isSelected());
                        if (option == NotifyDescriptor.YES_OPTION) {
                            ConnectionManager.getDefault().showConnectionDialog(o);
                            return o.getJDBCConnection() != null;
                        }
                    }
                    return false;
                });
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return false;
        }

        boolean isSkip(String dbName) {
            return Parser.onBoolean().parse(map.getProperty(dbName, Boolean.FALSE.toString()));
        }

        void setSkip(String dbName, boolean remember) {
            map.setProperty(dbName, Formatter.onBoolean().formatAsString(remember));
        }
    }

    private static <T> T execInEDT(Callable<T> callable) throws Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            RunnableFuture<T> x = new FutureTask(callable);
            SwingUtilities.invokeAndWait(x);
            return x.get();
        } else {
            return callable.call();
        }
    }

    private static final class FailSafeConnection implements Connection {

        @lombok.experimental.Delegate
        private final Connection delegate;
        private final String defaultSchema;

        public FailSafeConnection(@NonNull Connection delegate, String defaultSchema) {
            this.delegate = delegate;
            this.defaultSchema = defaultSchema;
        }

        @Override
        public String getSchema() throws SQLException {
            try {
                String result = delegate.getSchema();
                return result != null ? result : defaultSchema;
            } catch (SQLException | AbstractMethodError ex) {
                // occurs when :
                // - method is not yet implemented
                // - driver follows specs older than JDBC4 specs
                return defaultSchema;
            }
        }

        @Override
        public void close() throws SQLException {
            // Calling Connection#close() is forbidden
            // See DatabaseConnection#getJDBCConnection()
        }
    }

    private static final class DbConnStatusProperty extends PropertySupport.ReadOnly<String> {

        private final String dbName;

        public DbConnStatusProperty(String dbName) {
            super("Status", String.class, null, null);
            this.dbName = dbName;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return DbConnStatus.lookupByDisplayName(dbName).name();
        }
    }
}
