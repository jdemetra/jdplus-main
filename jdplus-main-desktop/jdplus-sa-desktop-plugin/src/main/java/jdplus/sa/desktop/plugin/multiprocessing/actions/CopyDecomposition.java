/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.sa.base.api.SaDictionaries;
import java.util.ArrayList;
import java.util.List;

import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action allowing export of the final decomposition
 *
 * @author Mats Maggi
 */
@ActionID(category = "SaProcessing", id = CopyDecomposition.ID)
@ActionRegistration(displayName = "#CTL_CopyDecomposition", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + Edit.PATH, position = 1410)
})
@Messages("CTL_CopyDecomposition=Copy Decomposition")
public final class CopyDecomposition extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.CopyDecomposition";

//    private final List<String> allFields;
//    private final JListSelection<String> fieldSelectionComponent;

    public CopyDecomposition() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_CopyDecomposition());
//        allFields = BasicConfiguration.allSeries(false, SaManager.instance.getProcessors());
//        fieldSelectionComponent = new JListSelection<>();
//        fieldSelectionComponent.setSourceHeader(new JLabel("Available items :"));
//        fieldSelectionComponent.setTargetHeader(new JLabel("Selected items :"));
//        fieldSelectionComponent.setBorder(new EmptyBorder(10, 10, 10, 10));
//        fieldSelectionComponent.setMinimumSize(new Dimension(400, 300));
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        if (ui == null || ui.getSelectionCount() != 1)
            enabled=false;
        else{
            SaNode node = ui.getSelection()[0];
            enabled=node.isProcessed();
        }
    }

    @Override
    protected void process(SaBatchUI cur) {
        List<String> decomp=new ArrayList<>();
        decomp.add(SaDictionaries.Y);
        decomp.add(SaDictionaries.SA);
        decomp.add(SaDictionaries.T);
        decomp.add(SaDictionaries.S);
        decomp.add(SaDictionaries.I);
        cur.copyComponents(decomp);
    }
}
