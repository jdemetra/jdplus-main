/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.base.api.timeseries.Ts;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Jean Palate
 */
public class StaticTableUI implements ItemUI<List<Ts>> {

    public StaticTableUI(){
    }


    @Override
    public JComponent getView(List<Ts> ts) {

        return TsViewToolkit.getGrid(ts);
    }

 }
