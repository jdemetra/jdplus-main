/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.base.api.processing.ProcDocument;
import javax.swing.JComponent;

/**
 *
 * @author Jean Palate
 * @param <D> Document type
 */
public class ContextualChartUI<D extends ProcDocument >implements ItemUI<ContextualIds<D>> {

     private final boolean fullNames;

    public ContextualChartUI(boolean fullNames){
        this.fullNames=fullNames;
    }


    @Override
    public JComponent getView(ContextualIds<D> info) {

        return TsViewToolkit.getChart(info.makeAllTs(fullNames));
    }

 }
