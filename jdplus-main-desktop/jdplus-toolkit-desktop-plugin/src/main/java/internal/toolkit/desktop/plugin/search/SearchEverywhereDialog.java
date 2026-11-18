/*
 * Copyright 2026 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the Licence;
 * you may not use this work except in compliance with the Licence.
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
package internal.toolkit.desktop.plugin.search;

import internal.toolkit.desktop.plugin.search.SearchEverywhereRenderer.Header;
import internal.toolkit.desktop.plugin.search.SearchEverywhereRenderer.Hit;
import internal.toolkit.desktop.plugin.search.SearchEverywhereRenderer.Item;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchProvider;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResult;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/** IntelliJ-like "Search Everywhere" popup backed by the registered desktop search providers. */
@NbBundle.Messages({
    "SearchEverywhereDialog.title=Search Everywhere",
    "SearchEverywhereDialog.hint=Type to search; press Shift twice to open this dialog",
    "SearchEverywhereDialog.searching=Searching...",
    "SearchEverywhereDialog.noResults=No results found",
    "SearchEverywhereDialog.recent=Recent",
    "SearchEverywhereDialog.clearRecent=Clear",
    "SearchEverywhereDialog.recentHint=Press Delete to remove the selected recent item"
})
final class SearchEverywhereDialog extends JDialog {

    private static final int MAX_RESULTS_PER_CATEGORY = 10;
    private static final int MAX_RECENT_RESULTS = 10;
    private static final int DEBOUNCE_MILLIS = 150;
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 200;
    private static final Preferences PREFERENCES = NbPreferences.forModule(SearchEverywhereDialog.class);
    private static final String WIDTH_PROPERTY = "searchEverywhere.width";
    private static final String HEIGHT_PROPERTY = "searchEverywhere.height";
    private static final RequestProcessor SEARCH_PROCESSOR =
            new RequestProcessor(SearchEverywhereDialog.class.getName(), 1, true);

    // Session-only state, accessed on the EDT
    private static final Deque<DesktopSearchResult> RECENT_RESULTS = new ArrayDeque<>();
    private static String lastQuery = "";
    private static SearchEverywhereDialog current;

    /** Opens the dialog, or brings it to front if already open. Must be called on the EDT. */
    static void open() {
        if (current != null && current.isDisplayable()) {
            current.toFront();
            current.searchField.requestFocusInWindow();
            return;
        }
        current = new SearchEverywhereDialog(WindowManager.getDefault().getMainWindow());
        current.setVisible(true);
    }

    private final JTextField searchField = new JTextField();
    private final DefaultListModel<Item> listModel = new DefaultListModel<>();
    private final JList<Item> resultList = new JList<>(listModel);
    private final JLabel statusLabel = new JLabel();
    private final SearchEverywhereRenderer renderer = new SearchEverywhereRenderer();
    private final SearchEverywhereEngine engine = new SearchEverywhereEngine(
            () -> Lookup.getDefault().lookupAll(DesktopSearchProvider.class),
            SEARCH_PROCESSOR,
            MAX_RESULTS_PER_CATEGORY);
    private final Timer debounceTimer = new Timer(DEBOUNCE_MILLIS, event -> startSearch());

    private SearchEverywhereDialog(Frame owner) {
        super(owner, Bundle.SearchEverywhereDialog_title(), false);
        setUndecorated(true);
        debounceTimer.setRepeats(false);
        initComponents();
        initBindings();
        initBounds(owner);
        restoreLastQuery();
    }

    private void restoreLastQuery() {
        if (lastQuery.isBlank()) {
            showRecentResults();
        } else {
            searchField.setText(lastQuery);
            searchField.selectAll();
        }
    }

    private void initComponents() {
        searchField.setFont(
                searchField.getFont().deriveFont(searchField.getFont().getSize2D() * 1.25f));
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onTextChanged();
            }
        });

        resultList.setFocusable(false);
        resultList.setSelectionModel(new HitSelectionModel());
        resultList.setCellRenderer(renderer);
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = resultList.locationToIndex(e.getPoint());
                if (index < 0 || !resultList.getCellBounds(index, index).contains(e.getPoint())) {
                    return;
                }
                if (listModel.get(index) instanceof Hit hit) {
                    execute(hit);
                } else if (renderer.isOnHeaderAction(resultList, index, e.getPoint())) {
                    clearRecentResults();
                }
            }
        });
        resultList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = resultList.locationToIndex(e.getPoint());
                boolean onAction = index >= 0 && renderer.isOnHeaderAction(resultList, index, e.getPoint());
                resultList.setCursor(onAction ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null);
            }
        });

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        statusLabel.setText(Bundle.SearchEverywhereDialog_hint());

        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UIManager.getColor("controlShadow")));

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("controlShadow")),
                BorderFactory.createEmptyBorder(
                        EdgeResizer.GRIP - 1, EdgeResizer.GRIP - 1, EdgeResizer.GRIP - 1, EdgeResizer.GRIP - 1)));
        content.add(searchField, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(statusLabel, BorderLayout.SOUTH);
        EdgeResizer resizer = new EdgeResizer(content);
        content.addMouseListener(resizer);
        content.addMouseMotionListener(resizer);
        setContentPane(content);
    }

    private void initBindings() {
        bind(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, KeyEvent.VK_ESCAPE, "close", this::close);
        bind(searchField, JComponent.WHEN_FOCUSED, KeyEvent.VK_UP, "previous", () -> moveSelection(-1));
        bind(searchField, JComponent.WHEN_FOCUSED, KeyEvent.VK_DOWN, "next", () -> moveSelection(1));
        bind(searchField, JComponent.WHEN_FOCUSED, KeyEvent.VK_ENTER, "execute", this::executeSelection);
        Action deleteNextChar = searchField.getActionMap().get(DefaultEditorKit.deleteNextCharAction);
        bind(searchField, JComponent.WHEN_FOCUSED, KeyEvent.VK_DELETE, "removeRecent", () -> {
            if (searchField.getText().isEmpty()) {
                removeSelectedRecentResult();
            } else if (deleteNextChar != null) {
                deleteNextChar.actionPerformed(new ActionEvent(searchField, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                close();
            }
        });
    }

    private static void bind(JComponent component, int condition, int keyCode, String name, Runnable action) {
        component.getInputMap(condition).put(KeyStroke.getKeyStroke(keyCode, 0), name);
        component.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void initBounds(Frame owner) {
        Rectangle ownerBounds = owner != null && owner.isShowing()
                ? owner.getBounds()
                : getGraphicsConfiguration().getBounds();
        int defaultWidth = Math.max(560, ownerBounds.width / 2);
        int width = clamp(PREFERENCES.getInt(WIDTH_PROPERTY, defaultWidth), MIN_WIDTH, ownerBounds.width);
        int height = clamp(PREFERENCES.getInt(HEIGHT_PROPERTY, 420), MIN_HEIGHT, ownerBounds.height);
        setBounds(
                ownerBounds.x + (ownerBounds.width - width) / 2, ownerBounds.y + ownerBounds.height / 6, width, height);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(Math.min(min, max), Math.min(value, max));
    }

    private void onTextChanged() {
        if (searchField.getText().isBlank()) {
            showRecentResults();
        } else {
            debounceTimer.restart();
        }
    }

    private void showRecentResults() {
        debounceTimer.stop();
        engine.cancel();
        List<Item> items = new ArrayList<>();
        if (!RECENT_RESULTS.isEmpty()) {
            items.add(new Header(Bundle.SearchEverywhereDialog_recent(), Bundle.SearchEverywhereDialog_clearRecent()));
            RECENT_RESULTS.forEach(result -> items.add(new Hit(result, "")));
        }
        setItems(items);
        statusLabel.setText(
                RECENT_RESULTS.isEmpty()
                        ? Bundle.SearchEverywhereDialog_hint()
                        : Bundle.SearchEverywhereDialog_recentHint());
    }

    private void removeSelectedRecentResult() {
        if (resultList.getSelectedValue() instanceof Hit hit) {
            int index = resultList.getSelectedIndex();
            RECENT_RESULTS.remove(hit.result());
            showRecentResults();
            int next = nextHitIndex(Math.min(index, listModel.size()) - 1, 1);
            if (next < 0) {
                next = nextHitIndex(listModel.size(), -1);
            }
            if (next >= 0) {
                resultList.setSelectedIndex(next);
                resultList.ensureIndexIsVisible(next);
            }
        }
    }

    private void clearRecentResults() {
        RECENT_RESULTS.clear();
        showRecentResults();
        searchField.requestFocusInWindow();
    }

    private void startSearch() {
        String text = searchField.getText();
        if (text.isBlank()) {
            return;
        }
        statusLabel.setText(Bundle.SearchEverywhereDialog_searching());
        AtomicReference<SearchEverywhereEngine.Search> search = new AtomicReference<>();
        search.set(engine.search(
                text,
                (groups, done) -> SwingUtilities.invokeLater(() -> {
                    SearchEverywhereEngine.Search value = search.get();
                    if (value != null && !value.isObsolete() && isDisplayable()) {
                        showResults(groups, done, text);
                    }
                })));
    }

    private void showResults(List<SearchEverywhereEngine.Group> groups, boolean done, String query) {
        List<Item> items = new ArrayList<>();
        for (SearchEverywhereEngine.Group group : groups) {
            items.add(new Header(group.categoryName()));
            group.results().forEach(result -> items.add(new Hit(result, query)));
        }
        setItems(items);
        if (!done) {
            statusLabel.setText(Bundle.SearchEverywhereDialog_searching());
        } else if (groups.isEmpty()) {
            statusLabel.setText(Bundle.SearchEverywhereDialog_noResults());
        } else {
            statusLabel.setText(" ");
        }
    }

    private void setItems(List<Item> items) {
        DesktopSearchResult selected = resultList.getSelectedValue() instanceof Hit hit ? hit.result() : null;
        listModel.clear();
        int selectedIndex = -1;
        for (Item item : items) {
            if (item instanceof Hit hit && hit.result().equals(selected)) {
                selectedIndex = listModel.size();
            }
            listModel.addElement(item);
        }
        if (selectedIndex < 0) {
            selectedIndex = nextHitIndex(-1, 1);
        }
        if (selectedIndex >= 0) {
            resultList.setSelectedIndex(selectedIndex);
            resultList.ensureIndexIsVisible(selectedIndex);
        }
    }

    private void moveSelection(int direction) {
        int index = nextHitIndex(resultList.getSelectedIndex(), direction);
        if (index >= 0) {
            resultList.setSelectedIndex(index);
            resultList.ensureIndexIsVisible(index);
        }
    }

    private int nextHitIndex(int from, int direction) {
        for (int i = from + direction; i >= 0 && i < listModel.size(); i += direction) {
            if (listModel.get(i) instanceof Hit) {
                return i;
            }
        }
        return -1;
    }

    private void executeSelection() {
        if (resultList.getSelectedValue() instanceof Hit hit) {
            execute(hit);
        }
    }

    private void execute(Hit hit) {
        addRecentResult(hit.result());
        close();
        SwingUtilities.invokeLater(hit.result().action());
    }

    private static void addRecentResult(DesktopSearchResult result) {
        RECENT_RESULTS.removeIf(o -> o.getFullDisplayName().equals(result.getFullDisplayName()));
        RECENT_RESULTS.addFirst(result);
        while (RECENT_RESULTS.size() > MAX_RECENT_RESULTS) {
            RECENT_RESULTS.removeLast();
        }
    }

    private void close() {
        if (!isDisplayable()) {
            return;
        }
        lastQuery = searchField.getText();
        PREFERENCES.putInt(WIDTH_PROPERTY, getWidth());
        PREFERENCES.putInt(HEIGHT_PROPERTY, getHeight());
        debounceTimer.stop();
        engine.cancel();
        dispose();
        if (current == this) {
            current = null;
        }
    }

    private final class EdgeResizer extends MouseAdapter {

        private static final int GRIP = 4;
        private static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8;

        private final JComponent component;
        private int edges;
        private Point start;
        private Rectangle startBounds;

        private EdgeResizer(JComponent component) {
            this.component = component;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            component.setCursor(getCursor(getEdges(e.getPoint())));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (edges == 0) {
                component.setCursor(null);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            edges = getEdges(e.getPoint());
            start = e.getLocationOnScreen();
            startBounds = SearchEverywhereDialog.this.getBounds();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            edges = 0;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (edges == 0) {
                return;
            }
            int dx = e.getXOnScreen() - start.x;
            int dy = e.getYOnScreen() - start.y;
            Rectangle bounds = new Rectangle(startBounds);
            if ((edges & EAST) != 0) {
                bounds.width = Math.max(MIN_WIDTH, startBounds.width + dx);
            }
            if ((edges & WEST) != 0) {
                bounds.width = Math.max(MIN_WIDTH, startBounds.width - dx);
                bounds.x = startBounds.x + startBounds.width - bounds.width;
            }
            if ((edges & SOUTH) != 0) {
                bounds.height = Math.max(MIN_HEIGHT, startBounds.height + dy);
            }
            if ((edges & NORTH) != 0) {
                bounds.height = Math.max(MIN_HEIGHT, startBounds.height - dy);
                bounds.y = startBounds.y + startBounds.height - bounds.height;
            }
            SearchEverywhereDialog.this.setBounds(bounds);
            SearchEverywhereDialog.this.validate();
        }

        private int getEdges(Point point) {
            int result = 0;
            if (point.y < GRIP) {
                result |= NORTH;
            } else if (point.y >= component.getHeight() - GRIP) {
                result |= SOUTH;
            }
            if (point.x < GRIP) {
                result |= WEST;
            } else if (point.x >= component.getWidth() - GRIP) {
                result |= EAST;
            }
            return result;
        }

        private static Cursor getCursor(int edges) {
            return switch (edges) {
                case NORTH -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                case SOUTH -> Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                case WEST -> Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                case EAST -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                case NORTH | WEST -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                case NORTH | EAST -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                case SOUTH | WEST -> Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                case SOUTH | EAST -> Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                default -> null;
            };
        }
    }

    private final class HitSelectionModel extends DefaultListSelectionModel {

        private HitSelectionModel() {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        @Override
        public void setSelectionInterval(int index0, int index1) {
            if (index1 >= 0 && index1 < listModel.size() && listModel.get(index1) instanceof Hit) {
                super.setSelectionInterval(index0, index1);
            }
        }
    }
}
