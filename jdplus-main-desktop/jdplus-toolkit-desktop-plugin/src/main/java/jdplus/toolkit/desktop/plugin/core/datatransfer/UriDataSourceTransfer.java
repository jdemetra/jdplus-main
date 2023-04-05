/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core.datatransfer;

import jdplus.toolkit.desktop.plugin.datatransfer.DataSourceTransferSpi;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransferManager;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransfers;
import jdplus.toolkit.base.tsp.DataSource;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;

import java.awt.datatransfer.Transferable;
import java.util.Optional;
import nbbrd.io.text.Parser;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class UriDataSourceTransfer implements DataSourceTransferSpi {

    @Override
    public boolean canHandle(Transferable t) {
        return getDataSource(t).isPresent();
    }

    @Override
    public boolean canHandle(Transferable t, String providerName) {
        Optional<DataSource> dataSource = getDataSource(t);
        return dataSource.isPresent() && dataSource.orElseThrow().getProviderName().equals(providerName);
    }

    @Override
    public Optional<DataSource> getDataSource(Transferable t) {
        return !DataTransferManager.get().isTssTransferable(t) ? DataTransfers.tryParse(t, Parser.of(DataSource::parse)) : Optional.empty();
    }

    @Override
    public Optional<DataSource> getDataSource(Transferable t, String providerName) {
        Optional<DataSource> result = getDataSource(t);
        return result.isPresent() && result.orElseThrow().getProviderName().equals(providerName) ? result : Optional.empty();
    }
}
