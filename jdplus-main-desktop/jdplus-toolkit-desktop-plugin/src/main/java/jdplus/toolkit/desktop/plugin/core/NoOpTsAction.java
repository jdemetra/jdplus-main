/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core;

import jdplus.toolkit.base.api.timeseries.Ts;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import jdplus.toolkit.desktop.plugin.TsActionOpenSpi;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class NoOpTsAction implements TsActionOpenSpi {

    @Override
    public String getName() {
        return "NullTsAction";
    }

    @Override
    public String getDisplayName() {
        return "Do nothing";
    }

    @Override
    public void open(Ts ts) {
    }
}
