/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.toolkit.desktop.plugin.ui.Menus;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Jean Palate
 */
@ActionID(category = "SaProcessing", id = Priority.ID)
@ActionRegistration(displayName = "#CTL_Priority", lazy=false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH, position = 1700, separatorBefore = 1699 ),
    @ActionReference(path = "Shortcuts", name = "p")
})
@NbBundle.Messages("CTL_Priority=Priority")
public final class Priority extends AbstractAction implements Presenter.Popup {
    
    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.Priority";

    public static final String PATH="/Priority";

    public Priority(){
        super(Bundle.CTL_Priority());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    }
 
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu=new JMenu(this);
        Menus.fillMenu(menu, MultiProcessingManager.CONTEXTPATH+PATH);
        return menu;
    }
}

