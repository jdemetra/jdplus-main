/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.processing;

import jdplus.toolkit.desktop.plugin.ui.processing.PooledItemUI;
import jdplus.toolkit.base.api.timeseries.TsData;


/**
 *
 * @author Jean Palate
 */
public class SiRatioUI extends PooledItemUI<TsData[], JSIView> {

    public SiRatioUI() {
        super(JSIView.class);
    }

    @Override
    protected void init(JSIView c, TsData[] information) {
        if (information.length == 2) {
            c.setSiData(information[0], information[1]);
        } else if (information.length == 1) {
            c.setData(information[0]);
        }
    }
}
