package jdplus.sa.desktop.plugin.multiprocessing.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.base.information.SaSpecificationMapping;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.xml.information.XmlInformationSet;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.interchange.Exportable;
import jdplus.toolkit.desktop.plugin.interchange.InterchangeManager;
import jdplus.toolkit.desktop.plugin.interchange.InterchangeSpi;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import nbbrd.design.ClassNameConstant;
import nbbrd.io.xml.bind.Jaxb;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(category = "SaProcessing", id = ExportEstSpecToFile.ID)
@ActionRegistration(displayName = "#CTL_ExportEstSpecToFile", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + EstSpecification.PATH, position = 1517),
    @ActionReference(path = MultiProcessingManager.LOCALPATH + EstSpecification.PATH, position = 1517)
})
@NbBundle.Messages("CTL_ExportEstSpecToFile=Export to file")
public final class ExportEstSpecToFile extends ActiveViewAction<SaBatchUI> {// implements Presenter.Popup, Presenter.Menu {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.ExportEstSpecToFile";

    public static final String PATH = EstSpecification.PATH + "/Export";
    private static final nbbrd.io.text.Formatter<InformationSet> INFORMATIONFORMATTER = Jaxb.Formatter.of(XmlInformationSet.class).asFormatter()
            .compose(o -> {
                XmlInformationSet result = new XmlInformationSet();
                result.copy(o);
                return result;
            });

    public ExportEstSpecToFile() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_ExportEstSpecToFile());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount() > 0
                && Arrays.stream(ui.getSelection()).anyMatch(n -> getEstSpec(n) != null);
    }

    @Override
    protected void process(SaBatchUI cur) {
        List<Exportable> exportables = new ArrayList<>();
        for (SaNode saNode : cur.getSelection()) {
            if (getEstSpec(saNode) != null) {
                exportables.add(new ExportEstSpecToFile.ExportableSpec(saNode));
            }
        }

        for (InterchangeSpi ics : InterchangeManager.get().all()) {
            if (ics.getName().contains("File")) {
                try {
                    if (ics.canExport(exportables)) {
                        ics.performExport(exportables);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ExportEstSpecToFile.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
        }
    }

    private static SaSpecification getEstSpec(SaNode saNode) {
        SaSpecification spec = null;
        if (saNode.getOutput() != null) {
            spec = saNode.getOutput().getDefinition().getEstimationSpec();
        }
        return spec;
    }

//    @Override
//    public JMenuItem getMenuPresenter() {
//        SaBatchUI cur = context();
//        List<Exportable> exportables = new ArrayList<>();
//        for (SaNode saNode : cur.getSelection()) {
//            if (getEstSpec(saNode) != null) {
//                exportables.add(new ExportRefSpecToFile.ExportableSpec(saNode));
//            }
//        }
//        JMenuItem result = InterchangeManager.get().newExportMenu(exportables);
//        result.setText(Bundle.CTL_ExportEstSpecToFile());
    ////        Menus.fillMenu((JMenu) result, MultiProcessingManager.CONTEXTPATH + PATH);
////        Menus.fillMenu((JMenu) result, MultiProcessingManager.LOCALPATH + PATH);
//        return result;
//    }
//
//    @Override
//    public JMenuItem getPopupPresenter() {
//        return getMenuPresenter();
//    }

    private static final class ExportableSpec implements Exportable {

        private final SaNode node;

        ExportableSpec(SaNode input) {
            this.node = input;
        }

        @Override
        public Config exportConfig() {
            SaSpecification spec = getEstSpec(node);
            InformationSet set;
            for (SaSpecificationMapping mapping : Lookup.getDefault().lookupAll(SaSpecificationMapping.class)) {
                set = mapping.write(spec, null, true, DemetraVersion.JD3);
                if (set != null) {
                    Config.Builder b = Config.builder(spec.getClass().getName(), node.getName(), "3.0.0")
                            .parameter("specification", INFORMATIONFORMATTER.formatAsString(set));
                    return b.build();
                }
            }
            return null;
        }
    }
}
