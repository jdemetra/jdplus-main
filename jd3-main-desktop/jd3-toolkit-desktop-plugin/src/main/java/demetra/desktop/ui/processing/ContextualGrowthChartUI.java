/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.desktop.ui.processing;

import demetra.processing.ProcDocument;
import javax.swing.JComponent;

/**
 *
 * @author Jean
 * @param <D>
 */
public class ContextualGrowthChartUI<D extends ProcDocument >implements ItemUI<ContextualIds<D>> {

    private final boolean fullNames;

    public ContextualGrowthChartUI(boolean fullNames){
        this.fullNames=fullNames;
    }


    @Override
    public JComponent getView(ContextualIds<D> info) {

        return TsViewToolkit.getGrowthChart(info.makeAllTs(fullNames));
    }

 }
