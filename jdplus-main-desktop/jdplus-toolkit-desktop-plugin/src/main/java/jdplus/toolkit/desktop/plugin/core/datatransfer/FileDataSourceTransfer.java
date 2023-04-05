/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core.datatransfer;

import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.datatransfer.DataSourceTransferSpi;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransfers;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import jdplus.toolkit.base.tsp.DataSourceLoader;
import jdplus.toolkit.base.tsp.FileBean;
import jdplus.toolkit.base.tsp.FileLoader;
import ec.util.list.swing.JLists;
import jdplus.toolkit.base.tsp.DataSource;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class FileDataSourceTransfer implements DataSourceTransferSpi {

    @Override
    public boolean canHandle(Transferable t) {
        Optional<File> file = DataTransfers.getSingleFile(t);
        return file.isPresent() && !getLoaders(file.orElseThrow()).isEmpty();
    }

    @Override
    public boolean canHandle(Transferable t, String providerName) {
        Optional<File> file = DataTransfers.getSingleFile(t);
        if (file.isPresent()) {
            Optional<FileLoader> loader = TsManager.get().getProvider(FileLoader.class, providerName);
            return loader.isPresent() && loader.orElseThrow().accept(file.orElseThrow());
        }
        return false;
    }

    @Override
    public Optional<DataSource> getDataSource(Transferable t) {
        File file = DataTransfers.getSingleFile(t).orElseThrow();
        List<FileLoader> loaders = getLoaders(file);
        Optional<FileLoader> loader = chooseLoader(loaders);
        if (loader.isPresent()) {
            FileBean bean = loader.orElseThrow().newBean();
            bean.setFile(file);
            if (DataSourceManager.get().getBeanEditor(loader.orElseThrow().getSource(), "Open data source").editBean(bean, Exceptions::printStackTrace)) {
                return Optional.of(loader.orElseThrow().encodeBean(bean));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<DataSource> getDataSource(Transferable t, String providerName) {
        File file = DataTransfers.getSingleFile(t).orElseThrow();
        FileLoader loader = TsManager.get().getProvider(FileLoader.class, providerName).orElseThrow();
        FileBean bean = loader.newBean();
        bean.setFile(file);
        if (DataSourceManager.get().getBeanEditor(loader.getSource(), "Open data source").editBean(bean, Exceptions::printStackTrace)) {
            return Optional.of(loader.encodeBean(bean));
        }
        return Optional.empty();
    }

    public static List<FileLoader> getLoaders(final File file) {
        return TsManager.get().getProviders()
                .filter(FileLoader.class::isInstance)
                .map(FileLoader.class::cast)
                .filter(o -> o.accept(file))
                .collect(Collectors.toList());
    }

    public static <T extends DataSourceLoader> Optional<T> chooseLoader(List<T> loaders) {
        if (loaders.size() == 1) {
            return Optional.of(loaders.get(0));
        }
        JComboBox cb = new JComboBox(loaders.toArray());
        cb.setRenderer(JLists.cellRendererOf(FileDataSourceTransfer::renderLoader));
        DialogDescriptor dd = new DialogDescriptor(cb, "Choose a loader");
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            return Optional.of((T) cb.getSelectedItem());
        }
        return Optional.empty();
    }

    private static void renderLoader(JLabel label, Object value) {
        DataSourceLoader loader = (DataSourceLoader) value;
        label.setText(loader.getDisplayName());
        label.setIcon(DataSourceManager.get().getIcon(loader.getSource(), BeanInfo.ICON_COLOR_16x16, false));
    }
}
