/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing.stats;

import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.TsViewToolkit;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;

/**
 *
 * @author Jean Palate
 */
public class RegressorsUI implements ItemUI<RegSarimaModel> {

    @Override
    public JComponent getView(RegSarimaModel information) {
        TsCollection items = createRegressors(information);
        return TsViewToolkit.getGrid(items);
    }

    private TsCollection createRegressors(RegSarimaModel information) {
        List<Ts> collection = new ArrayList<>();
        if (information != null) {
            TsDomain domain = information.getDescription().getSeries().getDomain();
            TsPeriod start = domain.getStartPeriod();
            Variable[] vars = information.getDescription().getVariables();
            if (vars != null) {
                for (Variable cur : vars) {
                    ITsVariable core = cur.getCore();
                    int dim = cur.dim();
                    if (dim > 1) {
                        FastMatrix matrix = Regression.matrix(domain, core);
                        for (int j = 0; j < dim; ++j) {
                            collection.add(Ts
                                    .builder()
                                    .moniker(TsMoniker.of())
                                    .name(core.description(j, domain))
                                    .data(TsData.of(start, matrix.column(j)))
                                    .build());
                        }
                    } else {
                        DataBlock x = Regression.x(domain, core);
                        collection.add(Ts
                                .builder()
                                .moniker(TsMoniker.of())
                                .name(cur.getName())
                                .data(TsData.ofInternal(start, x.getStorage()))
                                .build());
                    }
                }
            }
        }
        return TsCollection.of(collection);
    }
}
