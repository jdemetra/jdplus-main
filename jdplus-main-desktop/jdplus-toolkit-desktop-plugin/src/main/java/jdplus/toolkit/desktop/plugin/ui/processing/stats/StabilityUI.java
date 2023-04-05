/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.ui.processing.stats;

import jdplus.toolkit.desktop.plugin.components.tools.JStabilityView;
import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import java.util.Map;
import javax.swing.JComponent;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.MovingProcessing;

/**
 *
 * @author PALATEJ
 */
public class StabilityUI implements ItemUI<StabilityUI.Information> {

    @Override
    public JComponent getView(Information info) {
        JStabilityView view = new JStabilityView();
        boolean empty = true;
        MovingProcessing<Explorable> processing = info.getMovingProcessing();
        for (String item : info.getIds()) {
            Map<TsDomain, Double> movingInfo = processing.movingInfo(x
                    -> {
                RegressionItem reg=x.getData(item, RegressionItem.class);
                return reg == null ? Double.NaN : reg.getCoefficient();
            });
            if (isDefined(movingInfo)) {
                empty = false;
                int sep = item.lastIndexOf(Dictionary.SEP);
                view.add(sep > 0 ? item.substring(sep + 1) : item, movingInfo, null, false);
            }
        }
        if (empty) {
            view.showException(info.getExceptionMessage());

        } else {
            view.display();
        }

        return view;
    }

    @lombok.Value
    public static class Information {

        MovingProcessing<Explorable> movingProcessing;
        String[] Ids;
        String exceptionMessage;
    }

    private boolean isDefined(Map<TsDomain, Double> data) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        for (Map.Entry<TsDomain, Double> d : data.entrySet()) {
            if (Double.isFinite(d.getValue())) {
                return true;
            }
        }
        return false;
    }

}
