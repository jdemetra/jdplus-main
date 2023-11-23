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
package jdplus.toolkit.desktop.plugin.ui.mru;

import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.DataSourceLoader;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import lombok.NonNull;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@ActionID(category = "File", id = ProviderMruAction.ID)
@ActionRegistration(displayName = "#CTL_ProviderMruAction", lazy = false)
@ActionReferences({
        @ActionReference(path = "Menu/File", position = 810)
})
@Messages("CTL_ProviderMruAction=Open recent")
public final class ProviderMruAction extends AbstractAction implements Presenter.Popup, Presenter.Menu {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.ui.mru.ProviderMruAction";

    @Override
    public JMenuItem getMenuPresenter() {
        return new MruMenu(Bundle.CTL_ProviderMruAction());
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getMenuPresenter();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static final class MruMenu extends JMenu implements DynamicMenuContent, PropertyChangeListener {

        public MruMenu(String s) {
            super(s);
            MruList.getProvidersInstance().addWeakPropertyChangeListener(this);
            updateMenu();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource().equals(MruList.getProvidersInstance())) {
                updateMenu();
            }
        }

        @Override
        public JComponent[] getMenuPresenters() {
            return new JComponent[]{this};
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

        private void updateMenu() {
            removeAll();
            if (MruList.getProvidersInstance().isEmpty()) {
                this.setEnabled(false);
                return;
            }
            this.setEnabled(true);
            DataSourceManager support = DataSourceManager.get();
            for (SourceId o : MruList.getProvidersInstance()) {
                JMenuItem item = new JMenuItem(new OpenDataSource(o.getDataSource()));
                item.setText(o.getLabel());
                item.setIcon(getIcon(support, o.getDataSource()));
                item.setEnabled(isLoadable(o.getDataSource()));
                add(item);
            }
            add(new JSeparator());
            add(new JMenuItem(new AbstractAction("Clear") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    MruList.getProvidersInstance().clear();
                }
            }));
        }

        Icon getIcon(DataSourceManager support, DataSource dataSource) {
            return support.getIcon(dataSource, BeanInfo.ICON_COLOR_16x16, false);
        }

        boolean isLoadable(DataSource dataSource) {
            return TsManager.get()
                    .getProvider(DataSourceLoader.class, dataSource)
                    .filter(o -> !o.getDataSources().contains(dataSource))
                    .isPresent();
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class OpenDataSource extends AbstractAction {

        private final @NonNull DataSource dataSource;

        @Override
        public void actionPerformed(ActionEvent e) {
            TsManager.get().open(dataSource);
        }
    }
}
