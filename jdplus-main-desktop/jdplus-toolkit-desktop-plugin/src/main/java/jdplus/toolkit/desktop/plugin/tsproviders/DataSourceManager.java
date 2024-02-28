/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.desktop.plugin.tsproviders;

import internal.uihelpers.FixmeCollectionSupplier;
import jdplus.main.desktop.design.GlobalService;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.desktop.plugin.DemetraIcons;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.actions.Configurable;
import jdplus.toolkit.desktop.plugin.beans.BeanEditor;
import jdplus.toolkit.desktop.plugin.properties.ForwardingNodeProperty;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.properties.PropertySheetDialogBuilder;
import jdplus.toolkit.desktop.plugin.util.CollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.FrozenTsHelper;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import org.openide.ErrorManager;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@GlobalService
public final class DataSourceManager {

    @NonNull
    public static DataSourceManager get() {
        return LazyGlobalService.get(DataSourceManager.class, DataSourceManager::new);
    }

    private final CollectionSupplier<DataSourceProviderBuddy> providers;
    private final ImageStrategy images;
    private final SheetStrategy sheets;

    private DataSourceManager() {
        this.providers = FixmeCollectionSupplier.of(DataSourceProviderBuddy.class, DataSourceProviderBuddyLoader::load);
        this.images = new DefaultImageStrategy();
        this.sheets = new DefaultSheetStrategy();
    }

    private DataSourceProviderBuddy get(String providerName) {
        return providers
                .stream()
                .filter(o -> o.getProviderName().equals(providerName))
                .map(DataSourceProviderBuddy.class::cast)
                .findFirst()
                .orElse(NoOpDataSourceProviderBuddy.INSTANCE);
    }

    /**
     * Gets an icon for a provider.
     *
     * @param providerName
     * @param type
     * @param opened
     * @return an icon
     * @since 2.2.0
     */
    @NonNull
    public Image getImage(@NonNull String providerName, int type, boolean opened) {
        return images.getProviderImage(get(providerName), providerName, type, opened);
    }

    @NonNull
    public Icon getIcon(@NonNull String providerName, int type, boolean opened) {
        return ImageUtilities.image2Icon(getImage(providerName, type, opened));
    }

    @NonNull
    public Sheet getSheet(@NonNull String providerName) {
        return sheets.getProviderSheet(get(providerName), providerName);
    }

    @NonNull
    public BeanEditor getBeanEditor(@NonNull String providerName, @NonNull String title) {
        DataSourceProviderBuddy buddy = get(providerName);
        BeanEditor result = buddy.getBeanEditorOrNull(title);
        if (result != null) {
            return result;
        }
        return bean -> new PropertySheetDialogBuilder()
                .title(title)
                .icon(images.getProviderImage(buddy, providerName, BeanInfo.ICON_COLOR_16x16, false))
                .editSheet(sheets.getBeanSheet(buddy, providerName, bean));
    }

    /**
     * Gets a configurable for a provider.
     *
     * @param providerName
     * @return an optional configurable
     * @since 2.2.0
     */
    @NonNull
    public Optional<Configurable> getConfigurable(@NonNull String providerName) {
        return Optional.of(get(providerName))
                .filter(Configurable.class::isInstance)
                .map(Configurable.class::cast);
    }

    /**
     * Gets an icon for a data source.
     *
     * @param dataSource
     * @param type
     * @param opened
     * @return an icon
     * @since 2.2.0
     */
    @NonNull
    public Image getImage(@NonNull DataSource dataSource, int type, boolean opened) {
        return images.getDataSourceImage(get(dataSource.getProviderName()), dataSource, type, opened);
    }

    @NonNull
    public Icon getIcon(@NonNull DataSource dataSource, int type, boolean opened) {
        return ImageUtilities.image2Icon(getImage(dataSource, type, opened));
    }

    @NonNull
    public Sheet getSheet(@NonNull DataSource dataSource) {
        return sheets.getDataSourceSheet(get(dataSource.getProviderName()), dataSource);
    }

    /**
     * Gets an icon for a data set.
     *
     * @param dataSet
     * @param type
     * @param opened
     * @return an icon
     * @since 2.2.0
     */
    @NonNull
    public Image getImage(@NonNull DataSet dataSet, int type, boolean opened) {
        return images.getDataSetImage(get(dataSet.getDataSource().getProviderName()), dataSet, type, opened);
    }

    @NonNull
    public Icon getIcon(@NonNull DataSet dataSet, int type, boolean opened) {
        return ImageUtilities.image2Icon(getImage(dataSet, type, opened));
    }

    @NonNull
    public Sheet getSheet(@NonNull DataSet dataSet) {
        return sheets.getDataSetSheet(get(dataSet.getDataSource().getProviderName()), dataSet);
    }

    /**
     * Gets an icon for an exception thrown by a provider.
     *
     * @param providerName
     * @param ex
     * @param type
     * @param opened
     * @return an icon
     * @since 2.2.0
     */
    @NonNull
    public Image getImage(@NonNull String providerName, @NonNull IOException ex, int type, boolean opened) {
        return images.getErrorImage(get(providerName), providerName, ex, type, opened);
    }

    @NonNull
    public Icon getIcon(@NonNull String providerName, @NonNull IOException ex, int type, boolean opened) {
        return ImageUtilities.image2Icon(getImage(providerName, ex, type, opened));
    }

    @NonNull
    public Sheet getSheet(@NonNull String providerName, @NonNull IOException ex) {
        return sheets.getErrorSheet(get(providerName), providerName, ex);
    }

    /**
     * Gets an icon for a moniker.
     *
     * @param moniker
     * @param type
     * @param opened
     * @return an icon
     * @since 2.2.0
     */
    @NonNull
    public Image getImage(@NonNull TsMoniker moniker, int type, boolean opened) {
        TsMoniker original = FrozenTsHelper.getOriginalMoniker(moniker);
        if (original != null) {
            return images.getMonikerImage(get(original.getSource()), original, type, opened);
        } else {
            return images.getMonikerImage(get(moniker.getSource()), moniker, type, opened);
        }
    }

    @NonNull
    public Icon getIcon(@NonNull TsMoniker moniker, int type, boolean opened) {
        return ImageUtilities.image2Icon(getImage(moniker, type, opened));
    }

    @MightBePromoted
    private interface ImageStrategy {

        Image getProviderImage(DataSourceProviderBuddy buddy, String providerName, int type, boolean opened);

        Image getDataSourceImage(DataSourceProviderBuddy buddy, DataSource dataSource, int type, boolean opened);

        Image getDataSetImage(DataSourceProviderBuddy buddy, DataSet dataSet, int type, boolean opened);

        Image getErrorImage(DataSourceProviderBuddy buddy, String providerName, IOException ex, int type, boolean opened);

        Image getMonikerImage(DataSourceProviderBuddy buddy, TsMoniker moniker, int type, boolean opened);
    }

    @MightBePromoted
    private interface SheetStrategy {

        Sheet getProviderSheet(DataSourceProviderBuddy buddy, String providerName);

        Sheet getDataSourceSheet(DataSourceProviderBuddy buddy, DataSource dataSource);

        Sheet getDataSetSheet(DataSourceProviderBuddy buddy, DataSet dataSet);

        Sheet getErrorSheet(DataSourceProviderBuddy buddy, String providerName, IOException ex);

        Sheet getBeanSheet(DataSourceProviderBuddy buddy, String providerName, Object bean) throws IntrospectionException;
    }

    private static final class DefaultImageStrategy implements ImageStrategy {

        @Override
        public Image getProviderImage(DataSourceProviderBuddy buddy, String providerName, int type, boolean opened) {
            Image result = buddy.getIconOrNull(type, opened);
            return result != null ? result : DemetraIcons.DOCUMENT_16.getImageIcon().getImage();
        }

        @Override
        public Image getDataSourceImage(DataSourceProviderBuddy buddy, DataSource dataSource, int type, boolean opened) {
            Image result = buddy.getIconOrNull(dataSource, type, opened);
            return result != null ? result : getProviderImage(buddy, dataSource.getProviderName(), type, opened);
        }

        @Override
        public Image getDataSetImage(DataSourceProviderBuddy buddy, DataSet dataSet, int type, boolean opened) {
            Image result = buddy.getIconOrNull(dataSet, type, opened);
            if (result != null) {
                return result;
            }
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return ImageUtilities.loadImage("jdplus/toolkit/desktop/plugin/icons/folder.png", true);
                case SERIES:
                    return ImageUtilities.loadImage("jdplus/toolkit/desktop/plugin/icons/chart_line.png", true);
                default:
                    return getDataSourceImage(buddy, dataSet.getDataSource(), type, opened);
            }
        }

        @Override
        public Image getErrorImage(DataSourceProviderBuddy buddy, String providerName, IOException ex, int type, boolean opened) {
            Image result = buddy.getIconOrNull(ex, type, opened);
            return result != null ? result : DemetraIcons.EXCLAMATION_MARK_16.getImageIcon().getImage();
        }

        @Override
        public Image getMonikerImage(DataSourceProviderBuddy buddy, TsMoniker moniker, int type, boolean opened) {
            Image result = buddy.getIconOrNull(moniker, type, opened);
            return result != null ? result : getProviderImage(buddy, moniker.getSource(), type, opened);
        }
    }

    private static final class DefaultSheetStrategy implements SheetStrategy {

        @Override
        public Sheet getProviderSheet(DataSourceProviderBuddy buddy, String providerName) {
            List<Sheet.Set> result = buddy.getSheetOrNull();
            return sheetOf(result != null ? result : providerName(providerName));
        }

        @Override
        public Sheet getDataSourceSheet(DataSourceProviderBuddy buddy, DataSource dataSource) {
            List<Sheet.Set> result = buddy.getSheetOrNull(dataSource);
            return sheetOf(result != null ? result : dataSource(buddy, dataSource));
        }

        @Override
        public Sheet getDataSetSheet(DataSourceProviderBuddy buddy, DataSet dataSet) {
            List<Sheet.Set> result = buddy.getSheetOrNull(dataSet);
            return sheetOf(result != null ? result : dataSet(buddy, dataSet));
        }

        @Override
        public Sheet getErrorSheet(DataSourceProviderBuddy buddy, String providerName, IOException ex) {
            List<Sheet.Set> result = buddy.getSheetOrNull(ex);
            return sheetOf(result != null ? result : error(ex));
        }

        @Override
        public Sheet getBeanSheet(DataSourceProviderBuddy buddy, String providerName, Object bean) throws IntrospectionException {
            List<Sheet.Set> result = buddy.getSheetOfBeanOrNull(bean);
            return sheetOf(result != null ? result : bean(bean));
        }

        private static List<Sheet.Set> providerName(String providerName) {
            return TsManager.get()
                    .getProvider(DataSourceProvider.class, providerName)
                    .map(DefaultSheetStrategy::provider)
                    .orElseGet(Collections::emptyList);
        }

        private static List<Sheet.Set> provider(DataSourceProvider provider) {
            List<Sheet.Set> result = new ArrayList<>();
            NodePropertySetBuilder b = new NodePropertySetBuilder();
            b.with(String.class).select(provider, "getSource", null).display("Source").add();
            b.with(Boolean.class).select(provider, "isAvailable", null).display("Available").add();
            b.withBoolean().selectConst("Loadable", provider instanceof DataSourceLoader).add();
            b.withBoolean().selectConst("Files as source", provider instanceof FileLoader).add();
            result.add(b.build());
            return result;
        }

        private static List<Sheet.Set> bean(Object bean) throws IntrospectionException {
            return Stream.of(new BeanNode<>(bean).getPropertySets())
                    .map(DefaultSheetStrategy::sheetSetOf)
                    .collect(Collectors.toList());
        }

        private static List<Sheet.Set> error(IOException ex) {
            List<Sheet.Set> result = new ArrayList<>();
            NodePropertySetBuilder b = new NodePropertySetBuilder().name("IOException");

            int i = 0;
            Throwable current = ex;
            while (current != null) {
                b.reset("throwable" + i++).display(current.getClass().getSimpleName());
                b.with(String.class).selectConst("Type", current.getClass().getName()).add();
                b.with(String.class).selectConst("Message", current.getMessage()).add();
                result.add(b.build());
                current = current.getCause();
            }

            return result;
        }

        private static List<Sheet.Set> dataSource(DataSourceProviderBuddy buddy, DataSource dataSource) {
            List<Sheet.Set> result = new ArrayList<>();
            NodePropertySetBuilder b = new NodePropertySetBuilder().name("DataSource");
            b.with(String.class).select(dataSource, "getProviderName", null).display("Source").add();
            b.with(String.class).select(dataSource, "getVersion", null).display("Version").add();
            Optional<DataSourceLoader> loader = TsManager.get().getProvider(DataSourceLoader.class, dataSource);
            if (loader.isPresent()) {
                Object bean = loader.orElseThrow().decodeBean(dataSource);
                try {
                    List<Sheet.Set> sheetOfBeanOrNull = buddy.getSheetOfBeanOrNull(bean);
                    if (sheetOfBeanOrNull == null) {
                        sheetOfBeanOrNull = bean(bean);
                    }
                    if (sheetOfBeanOrNull != null) {
                        sheetOfBeanOrNull.stream()
                                .flatMap(set -> Stream.of(set.getProperties()))
                                .map(ForwardingNodeProperty::readOnly)
                                .forEach(b::add);
                    }
                } catch (IntrospectionException ex) {
                    ErrorManager.getDefault().log(ex.getMessage());
                }
            }
            result.add(b.build());
            return result;
        }

        private static List<Sheet.Set> dataSet(DataSourceProviderBuddy buddy, DataSet dataSet) {
            List<Sheet.Set> result = new ArrayList<>(dataSource(buddy, dataSet.getDataSource()));
            NodePropertySetBuilder b = new NodePropertySetBuilder().name("DataSet");
            b.withEnum(DataSet.Kind.class).select(dataSet, "getKind", null).display("Kind").add();
            dataSet.getParameters().forEach((k, v) -> b.with(String.class).selectConst(k, v).add());
            result.add(b.build());
            return result;
        }

        private static Sheet sheetOf(List<Sheet.Set> sets) {
            Sheet result = new Sheet();
            sets.forEach(result::put);
            return result;
        }

        private static Sheet.Set sheetSetOf(Node.PropertySet o) {
            Sheet.Set set = Sheet.createPropertiesSet();
            set.put(o.getProperties());
            return set;
        }
    }

    private enum NoOpDataSourceProviderBuddy implements DataSourceProviderBuddy {
        INSTANCE;

        @Override
        public String getProviderName() {
            return "NoOp";
        }
    }
}
