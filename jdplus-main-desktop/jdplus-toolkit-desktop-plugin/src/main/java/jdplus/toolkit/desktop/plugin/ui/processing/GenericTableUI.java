/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.base.api.information.BasicInformationExtractor;
import jdplus.toolkit.base.api.processing.ProcDocument;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Jean Palate
 * @param <D>
 */
public class GenericTableUI<D extends ProcDocument> implements ItemUI<D> {

    private final String[] ids;
    private final boolean fullNames;

    public GenericTableUI(boolean fullNames, String... ids) {
        this.ids = ids.clone();
        this.fullNames = fullNames;
    }

    @Override
    public JComponent getView(D doc) {

        List<Ts> items = new ArrayList<>();
        for (String s : ids) {
            TsMoniker moniker = TsDynamicProvider.monikerOf(doc, s);
            Ts x = TsFactory.getDefault().makeTs(moniker, TsInformationType.All);
            if (!fullNames) {
                int idx = s.lastIndexOf(BasicInformationExtractor.SEP);
                if (idx > 0) {
                    x = x.withName(s.substring(idx + 1));
                }
            }
            if (!x.getData().isEmpty()) {
                items.add(x);
            }
        }
        return TsViewToolkit.getGrid(items);
    }

}
