/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.text.desktop.plugin;

import demetra.desktop.tsproviders.DataSourceProviderBuddy;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(DataSourceProviderBuddy.class)
public final class UscbProviderBuddy implements DataSourceProviderBuddy {

    private static final String SOURCE = "USCB";

    @Override
    public String getProviderName() {
        return SOURCE;
    }
}
