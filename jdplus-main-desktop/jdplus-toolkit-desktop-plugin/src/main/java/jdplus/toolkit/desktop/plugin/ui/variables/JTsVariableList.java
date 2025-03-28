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
package jdplus.toolkit.desktop.plugin.ui.variables;

import ec.util.grid.swing.XTable;
import ec.util.table.swing.JTables;
import ec.util.various.swing.JCommand;
import jdplus.main.desktop.design.SwingAction;
import jdplus.main.desktop.design.SwingComponent;
import jdplus.toolkit.base.api.timeseries.*;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import jdplus.toolkit.base.api.util.DefaultNameValidator;
import jdplus.toolkit.base.api.util.INameValidator;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.tsp.util.ShortLivedCache;
import jdplus.toolkit.base.tsp.util.ShortLivedCachingLoader;
import jdplus.toolkit.desktop.plugin.DemetraBehaviour;
import jdplus.toolkit.desktop.plugin.NamedService;
import jdplus.toolkit.desktop.plugin.TsActionManager;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.components.parts.HasTsAction;
import jdplus.toolkit.desktop.plugin.components.parts.HasTsActionSupport;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransferManager;
import jdplus.toolkit.desktop.plugin.jfreechart.TsSparklineCellRenderer;
import jdplus.toolkit.desktop.plugin.notification.NotifyUtil;
import jdplus.toolkit.desktop.plugin.util.ActionMaps;
import jdplus.toolkit.desktop.plugin.util.InputMaps;
import jdplus.toolkit.desktop.plugin.util.KeyStrokes;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.SkipProcessing;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.Duration;
import java.util.function.UnaryOperator;

/**
 * @author Jean Palate
 */
@SwingComponent
@SkipProcessing(target = SwingComponent.class, reason = "parameters in constructor")
public final class JTsVariableList extends JComponent implements HasTsAction {

    @SwingAction
    public static final String DELETE_ACTION = "delete";

    @SwingAction
    public static final String CLEAR_ACTION = "clear";

    @SwingAction
    public static final String SELECT_ALL_ACTION = "selectAll";

    @SwingAction
    public static final String RENAME_ACTION = "rename";

    @SwingAction
    public static final String OPEN_ACTION = "open";

    public static final String VARIABLES_CHANGED = "variables_changed";

    private final XTable table;
    private final TsDataSuppliers variables;

    @lombok.experimental.Delegate
    private final HasTsAction tsAction;

    public JTsVariableList(TsDataSuppliers vars) {
        this.variables = vars;
        this.table = buildTable();
        this.tsAction = HasTsActionSupport.of(this::firePropertyChange);

        registerActions();
        registerInputs();
        enableOpenOnDoubleClick();
        enablePopupMenu();

        setLayout(new BorderLayout());
        add(NbComponents.newJScrollPane(table), BorderLayout.CENTER);

    }

    private JPopupMenu buildPopupMenu() {
        ActionMap actionMap = getActionMap();

        JMenu result = new JMenu();

        JMenuItem item;

        item = new JMenuItem(actionMap.get(OPEN_ACTION));
        item.setText("Open");
        item.setAccelerator(KeyStrokes.OPEN.get(0));
        item.setFont(item.getFont().deriveFont(Font.BOLD));
        result.add(item);

        item = buildOpenWithMenu();
        item.setText("Open with");
        result.add(item);

        item = new JMenuItem(actionMap.get(RENAME_ACTION));
        item.setText("Rename...");
        result.add(item);

        item = new JMenuItem(RenameUsingDescriptionCommand.INSTANCE.toAction(this));
        item.setText("Rename using description");
        result.add(item);

        result.addSeparator();

        item = new JMenuItem(actionMap.get(DELETE_ACTION));
        item.setText("Remove");
        item.setAccelerator(KeyStrokes.DELETE.get(0));
        result.add(item);

        item = new JMenuItem(actionMap.get(CLEAR_ACTION));
        item.setText("Clear");
        item.setAccelerator(KeyStrokes.CLEAR.get(0));
        result.add(item);

        return result.getPopupMenu();
    }

    private String[] names(int[] pos) {
        String[] n = new String[pos.length];
        CustomTableModel model = (CustomTableModel) table.getModel();
        for (int i = 0; i < pos.length; ++i) {
            n[i] = model.names[pos[i]];
        }
        return n;
    }

    private XTable buildTable() {
        final XTable result = new XTable();
        result.setNoDataRenderer(new XTable.DefaultNoDataRenderer("Drop data here", "Drop data here"));

        result.setDefaultRenderer(TsData.class, new TsSparklineCellRenderer());
        result.setDefaultRenderer(TsPeriod.class, JTables.cellRendererOf(JTsVariableList::renderPeriod));
        result.setDefaultRenderer(TsMoniker.class, JTables.cellRendererOf(JTsVariableList::renderMoniker));
        result.setDefaultRenderer(String.class, JTables.cellRendererOf(JTsVariableList::renderMultiLine));

        result.setModel(new CustomTableModel());
        JTables.setWidthAsPercentages(result, .1, .1, .1, .1, .3);

        result.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(result.getModel());
        result.setRowSorter(sorter);
        result.setDragEnabled(true);
        result.setTransferHandler(new TsVariableTransferHandler());
        result.setFillsViewportHeight(true);

        return result;
    }

    public class TsVariableTransferHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return null;
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            boolean result = DataTransferManager.get().canImport(support.getTransferable());
            if (result && support.isDrop()) {
                support.setDropAction(COPY);
            }
            return result;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (DataTransferManager.get()
                    .toTsCollectionStream(support.getTransferable())
                    .map(col -> col.load(TsInformationType.All, TsManager.get()))
                    .filter(col -> !col.isEmpty())
                    .peek(JTsVariableList.this::appendTsVariables)
                    .count() > 0) {
                firePropertyChange(VARIABLES_CHANGED, false, true);
                return true;
            } else {
                return false;
            }
        }
    }

    public void appendTsVariables(TsCollection coll) {
        for (Ts s : coll) {
            String name = variables.nextName();
            TsDataSupplier var;
            if (s.getMoniker().isUserDefined()) {
                var = new StaticTsDataSupplier(s.getData());
            } else {
                var = new DynamicTsDataSupplier(s.getMoniker(), s.getData());
            }
            variables.set(name, var);

        }
        ((CustomTableModel) table.getModel()).fireTableStructureChanged();
    }

    private static final ShortLivedCache<TsMoniker, String> DESCRIPTION_CACHE = ShortLivedCachingLoader.get().ofTtl(Duration.ofMinutes(5));

    // FIXME: this is a quick&dirty fix; should be replaced by a proper cache+async solution
    private static String getDescription(TsMoniker moniker) {
        String result = DESCRIPTION_CACHE.get(moniker);
        if (result == null) {
            // Possibly slow retrieval
            result = TsFactory.getDefault().makeTs(moniker, TsInformationType.None).getName();
            DESCRIPTION_CACHE.put(moniker, result);
        }
        return result;
    }

    private static void renderPeriod(JLabel label, TsPeriod value) {
        label.setHorizontalAlignment(SwingConstants.TRAILING);
    }

    private static void renderMoniker(JLabel label, TsMoniker value) {
        renderMultiLine(label, value != null ? getDescription(value) : null);
    }

    private static void renderMultiLine(JLabel label, String value) {
        if (value == null) {
            label.setText(null);
            label.setToolTipText(null);
        } else if (value.isEmpty()) {
            label.setText(" ");
            label.setToolTipText(null);
        } else if (value.startsWith("<html>")) {
            label.setText(value);
            label.setToolTipText(value);
        } else {
            label.setText(MultiLineNameUtil.join(value));
            label.setToolTipText(MultiLineNameUtil.toHtml(value));
        }
    }

    private static final String[] COLUMNS = new String[]{"Name", "Description", "Type", "Start", "End", "Data"};

    private class CustomTableModel extends AbstractTableModel {

        private String[] names;

        @Override
        public void fireTableStructureChanged() {
            names = variables.getNames();
            super.fireTableStructureChanged();
        }

        public CustomTableModel() {
            names = variables.getNames();
        }

        @Override
        public int getRowCount() {
            return names.length;
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String name = names[rowIndex];

            return switch (columnIndex) {
                case 0 ->
                    name;
                case 1 ->
                    variables.get(name) instanceof DynamicTsDataSupplier supplier ? supplier.getMoniker() : null;
                case 2 ->
                    variables.get(name) instanceof DynamicTsDataSupplier ? "Dynamic" : "Static";
                case 3 ->
                    variables.get(name).get().getStart();
                case 4 ->
                    variables.get(name).get().getEnd().previous();
                case 5 ->
                    variables.get(name).get();
                default ->
                    null;
            };
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0, 2 ->
                    String.class;
                case 1 ->
                    TsMoniker.class;
                case 3, 4 ->
                    TsPeriod.class;
                case 5 ->
                    TsData.class;
                default ->
                    super.getColumnClass(columnIndex);
            };
        }
    }

    private static final class VarName extends NotifyDescriptor.InputLine {

        VarName(final TsDataSuppliers vars, String title, String text, final String oldname) {
            super(title, text, NotifyDescriptor.QUESTION_MESSAGE, NotifyDescriptor.OK_CANCEL_OPTION);

            setInputText(oldname);
            textField.addKeyListener(new KeyListener() {
                // To handle VK_ENTER !!!
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && !textField.getInputVerifier().verify(textField)) {
                        e.consume();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
            textField.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    JTextField txt = (JTextField) input;
                    String name = txt.getText();
                    if (name.equals(oldname)) {
                        return true;
                    }
                    if (vars.contains(name)) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(name + " is in use. You should choose another name!");
                        DialogDisplayer.getDefault().notify(nd);
                        return false;
                    }
                    if (!vars.getNameValidator().accept(name)) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(vars.getNameValidator().getLastError());
                        DialogDisplayer.getDefault().notify(nd);
                        return false;
                    }
                    return true;
                }
            });
        }
    }

    private void registerActions() {
        ActionMap am = getActionMap();
        am.put(OPEN_ACTION, OpenCommand.INSTANCE.toAction(this));
        am.put(RENAME_ACTION, RenameCommand.INSTANCE.toAction(this));
        am.put(DELETE_ACTION, DeleteCommand.INSTANCE.toAction(this));
        am.put(CLEAR_ACTION, ClearCommand.INSTANCE.toAction(this));
        ActionMaps.copyEntries(am, false, table.getActionMap());
    }

    private void registerInputs() {
        InputMap im = getInputMap();
        KeyStrokes.putAll(im, KeyStrokes.OPEN, OPEN_ACTION);
        KeyStrokes.putAll(im, KeyStrokes.DELETE, DELETE_ACTION);
        KeyStrokes.putAll(im, KeyStrokes.CLEAR, CLEAR_ACTION);
        InputMaps.copyEntries(im, false, table.getInputMap());
    }

    private void enableOpenOnDoubleClick() {
        ActionMaps.onDoubleClick(getActionMap(), OPEN_ACTION, table);
    }

    private void enablePopupMenu() {
        table.setComponentPopupMenu(buildPopupMenu());
    }

    private JMenu buildOpenWithMenu() {
        JMenu result = new JMenu(OpenWithCommand.INSTANCE.toAction(this));

        for (NamedService o : TsActionManager.get().getOpenActions()) {
            JMenuItem item = new JMenuItem(new OpenWithItemCommand(o).toAction(this));
            item.setName(o.getName());
            item.setText(o.getDisplayName());
            result.add(item);
        }

        return result;
    }

    private static String getSelectedVariable(JTsVariableList c) {
        if (c.table.getSelectedRowCount() == 1) {
            int idx = c.table.convertRowIndexToModel(c.table.getSelectedRow());
            return c.variables.getNames()[idx];
        }
        return null;
    }

    private static Ts toTs(TsDataSuppliers vars, String name) {
        TsDataSupplier variable = vars.get(name);
        if (variable == null) {
            return null;
        }
        return variable instanceof DynamicTsDataSupplier dynamicSupplier
                ? TsFactory.getDefault().makeTs(dynamicSupplier.getMoniker(), TsInformationType.None)
                : Ts.of(name, variable.get());
    }

    private static final class OpenCommand extends JCommand<JTsVariableList> {

        public static final OpenCommand INSTANCE = new OpenCommand();

        @Override
        public void execute(JTsVariableList c) {
            String actionName = c.getTsAction();
            if (actionName == null) {
                actionName = DemetraBehaviour.get().getTsActionName();
            }
            String selectedVariable = getSelectedVariable(c);
            if (selectedVariable != null) {
                Ts ts = toTs(c.variables, selectedVariable);
                if (ts != null) {
                    TsActionManager.get().openWith(ts, actionName);
                }
            }
        }

        @Override
        public boolean isEnabled(@NonNull JTsVariableList c) {
            return getSelectedVariable(c) != null;
        }

        @Override
        public @NonNull
        ActionAdapter toAction(@NonNull JTsVariableList c) {
            return super.toAction(c).withWeakListSelectionListener(c.table.getSelectionModel());
        }
    }

    private static final class OpenWithCommand extends JCommand<JTsVariableList> {

        public static final OpenWithCommand INSTANCE = new OpenWithCommand();

        @Override
        public void execute(@NonNull JTsVariableList ignore) {
            // do nothing
        }

        @Override
        public boolean isEnabled(JTsVariableList c) {
            return c.table.getSelectedRowCount() == 1;
        }

        @Override
        public @NonNull
        ActionAdapter toAction(@NonNull JTsVariableList c) {
            return super.toAction(c).withWeakListSelectionListener(c.table.getSelectionModel());
        }
    }

    @lombok.AllArgsConstructor
    private static final class OpenWithItemCommand extends JCommand<JTsVariableList> {

        private final NamedService tsAction;

        @Override
        public void execute(JTsVariableList c) {
            Ts ts = toTs(c.variables, getSelectedVariable(c));
            if (ts != null) {
                TsActionManager.get().openWith(ts, tsAction.getName());
            }
        }
    }

    private static final class RenameCommand extends JCommand<JTsVariableList> {

        public static final RenameCommand INSTANCE = new RenameCommand();

        @Override
        public void execute(JTsVariableList c) {
            String oldName = c.names(c.table.getSelectedRows())[0], newName;
            VarName nd = new VarName(c.variables, "New name:", "Please enter the new name", oldName);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }
            newName = nd.getInputText();
            if (newName.equals(oldName)) {
                return;
            }
            c.variables.rename(oldName, newName);
            c.firePropertyChange(VARIABLES_CHANGED, false, true);
            ((CustomTableModel) c.table.getModel()).fireTableStructureChanged();
        }

        @Override
        public boolean isEnabled(JTsVariableList c) {
            return c.table.getSelectedRowCount() == 1;
        }

        @Override
        public @NonNull
        ActionAdapter toAction(@NonNull JTsVariableList c) {
            return super.toAction(c).withWeakListSelectionListener(c.table.getSelectionModel());
        }
    }

    private static final class RenameUsingDescriptionCommand extends JCommand<JTsVariableList> {

        public static final RenameUsingDescriptionCommand INSTANCE = new RenameUsingDescriptionCommand();

        @Override
        public void execute(JTsVariableList c) {
            UnaryOperator<String> nameFixer = getNameFixer(c.variables.getNameValidator(), '_');

            for (String oldName : c.names(c.table.getSelectedRows())) {
                TsDataSupplier supplier = c.variables.get(oldName);
                if (supplier instanceof DynamicTsDataSupplier dynamic) {
                    String description = getDescription(dynamic.getMoniker());
                    c.variables.rename(oldName, nameFixer.apply(description));
                    c.firePropertyChange(VARIABLES_CHANGED, false, true);
                }
            }

            ((CustomTableModel) c.table.getModel()).fireTableStructureChanged();
        }

        private static boolean isReplaceable(char[] invalidChars, char fallbackChar) {
            return String.valueOf(invalidChars).indexOf(fallbackChar) == -1;
        }

        private static String replaceChars(String text, char[] invalidChars, char fallbackChar) {
            for (char c : invalidChars) {
                text = text.replace(c, fallbackChar);
            }
            return text;
        }

        @SuppressWarnings("SameParameterValue")
        private static UnaryOperator<String> getNameFixer(INameValidator validator, char fallbackChar) {
            if (validator instanceof DefaultNameValidator defaultNameValidator) {
                char[] invalidChars = defaultNameValidator.getInvalidChars();
                if (isReplaceable(invalidChars, fallbackChar)) {
                    return name -> replaceChars(name, invalidChars, fallbackChar);
                }
            }
            return UnaryOperator.identity();
        }

        @Override
        public boolean isEnabled(JTsVariableList c) {
            return !c.table.getSelectionModel().isSelectionEmpty();
        }

        @Override
        public @NonNull
        ActionAdapter toAction(@NonNull JTsVariableList c) {
            return new ActionAdapter(c) {
                @Override
                public void handleException(ActionEvent event, Exception ex) {
                    NotifyUtil.error("Renaming", "Failed to rename item", ex);
                }
            }.withWeakListSelectionListener(c.table.getSelectionModel());
        }
    }

    private static final class DeleteCommand extends JCommand<JTsVariableList> {

        public static final DeleteCommand INSTANCE = new DeleteCommand();

        @Override
        public void execute(@NonNull JTsVariableList c) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation("Are you sure you want to delete the selected items?", NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }

            for (String s : c.names(c.table.getSelectedRows())) {
                c.variables.remove(s);
            }
            c.firePropertyChange(VARIABLES_CHANGED, false, true);
            ((CustomTableModel) c.table.getModel()).fireTableStructureChanged();
        }

        @Override
        public boolean isEnabled(JTsVariableList c) {
            return c.table.getSelectedRowCount() > 0;
        }

        @Override
        public @NonNull
        ActionAdapter toAction(@NonNull JTsVariableList c) {
            return super.toAction(c).withWeakListSelectionListener(c.table.getSelectionModel());
        }
    }

    private static final class ClearCommand extends JCommand2<JTsVariableList> {

        public static final ClearCommand INSTANCE = new ClearCommand();

        @Override
        public void execute(@NonNull JTsVariableList c) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation("Are you sure you want to clear all items?", NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }

            c.variables.clear();
            c.firePropertyChange(VARIABLES_CHANGED, false, true);
            ((CustomTableModel) c.table.getModel()).fireTableStructureChanged();
        }

        @Override
        public boolean isEnabled(JTsVariableList c) {
            return c.table.getRowCount() > 0;
        }

        @Override
        public @NonNull
        ActionAdapter2 toAction(@NonNull JTsVariableList c) {
            return super.toAction(c).withWeakTableModelListener(c.table.getModel());
        }
    }

    @MightBePromoted
    private static abstract class JCommand2<T> extends JCommand<T> {

        @Override
        public @NonNull
        ActionAdapter2 toAction(@NonNull T component) {
            return new ActionAdapter2(component);
        }

        public class ActionAdapter2 extends ActionAdapter {

            public ActionAdapter2(@NonNull T component) {
                super(component);
            }

            @NonNull
            @Override
            public ActionAdapter2 withWeakTableModelListener(@NonNull TableModel source) {
                TableModelListener realListener = evt -> refreshActionState();
                putValue("TableModelListener", realListener);
                source.addTableModelListener(new WeakTableModelListener(realListener) {
                    @Override
                    protected void unregister(@NonNull Object source) {
                        ((TableModel) source).removeTableModelListener(this);
                    }
                });
                return this;
            }

            private abstract static class WeakTableModelListener extends WeakEventListener<TableModelListener> implements TableModelListener {

                public WeakTableModelListener(@NonNull TableModelListener delegate) {
                    super(delegate);
                }

                @Override
                public void tableChanged(TableModelEvent e) {
                    TableModelListener listener = delegate.get();
                    if (listener != null) {
                        listener.tableChanged(e);
                    } else {
                        unregister(e.getSource());
                    }
                }
            }
        }
    }
}
