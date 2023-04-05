/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.base.api.processing.ProcDocument;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Jean Palate
 * @param <D> Document type
 */
public class GenericChartUI<D extends ProcDocument >implements ItemUI<D> {

    private final List<String> names_;
    private final boolean full_;

    public GenericChartUI(boolean fullNames, String...names){
        names_= Arrays.asList(names);
        full_=fullNames;
    }


    @Override
    public JComponent getView(D doc) {

        List<Ts> items=new ArrayList<>();
        for (String s : names_){
            TsMoniker moniker = TsDynamicProvider.monikerOf(doc, s);
            Ts x =  TsFactory.getDefault().makeTs(moniker, TsInformationType.All);
            if (!x.getData().isEmpty())
                items.add(x);
       }
        return TsViewToolkit.getChart(items);
    }

 }
