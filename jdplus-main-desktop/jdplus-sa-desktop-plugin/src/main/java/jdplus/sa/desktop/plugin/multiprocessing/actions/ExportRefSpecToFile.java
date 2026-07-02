package jdplus.sa.desktop.plugin.multiprocessing.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.xml.information.XmlInformationSet;
import jdplus.toolkit.desktop.plugin.Config;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.Action.NAME;
import jdplus.sa.base.information.SaSpecificationMapping;
import jdplus.toolkit.base.api.DemetraVersion;
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
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = ExportRefSpecToFile.ID)
@ActionRegistration(displayName = "#CTL_ExportRefSpecToFile", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + RefSpecification.PATH, position = 1517),
    @ActionReference(path = MultiProcessingManager.LOCALPATH + RefSpecification.PATH, position = 1517)
})
@Messages("CTL_ExportRefSpecToFile=Export to file")
public final class ExportRefSpecToFile extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.ExportRefSpecToFile";

    public static final String PATH = RefSpecification.PATH + "/Export";
    private static final nbbrd.io.text.Formatter<InformationSet> INFORMATIONFORMATTER = Jaxb.Formatter.of(XmlInformationSet.class).asFormatter()
            .compose(o -> {
                XmlInformationSet result = new XmlInformationSet();
                result.copy(o);
                return result;
            });

    public ExportRefSpecToFile() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_ExportRefSpecToFile());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount() > 0
                && Arrays.stream(ui.getSelection()).anyMatch(n -> getRefSpec(n) != null);
    }

    @Override
    protected void process(SaBatchUI cur) {
        List<Exportable> exportables = new ArrayList<>();
        for (SaNode saNode : cur.getSelection()) {
            if (getRefSpec(saNode) != null) {
                exportables.add(new ExportRefSpecToFile.ExportableSpec(saNode));
            }
        }

        for (InterchangeSpi ics : InterchangeManager.get().all()) {
            if (ics.getName().contains("File")) {
                try {
                    if (ics.canExport(exportables)) {
                        ics.performExport(exportables);
                        break;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ExportRefSpecToFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static SaSpecification getRefSpec(SaNode saNode) {
        SaSpecification spec = null;
        if (saNode.getOutput() != null) {
            spec = saNode.getOutput().getDefinition().getDomainSpec();
        }
        return spec;
    }

//    @Override
//    public JMenuItem getMenuPresenter() {
//        SaBatchUI cur = context();
//        List<Exportable> exportables = new ArrayList<>();
//        for (SaNode saNode : cur.getSelection()) {
//            if (getRefSpec(saNode) != null) {
//                exportables.add(new ExportRefSpecToFile.ExportableSpec(saNode));
//            }
//        }
//        JMenuItem result = InterchangeManager.get().newExportMenu(exportables);
//        result.setText(Bundle.CTL_ExportRefSpecToFile());
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
            SaSpecification spec = getRefSpec(node);
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
