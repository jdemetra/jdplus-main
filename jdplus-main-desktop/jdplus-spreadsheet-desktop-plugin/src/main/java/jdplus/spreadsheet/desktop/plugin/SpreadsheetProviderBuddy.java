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
package jdplus.spreadsheet.desktop.plugin;

import jdplus.spreadsheet.base.api.SpreadSheetBean;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.FileLoader;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceProviderBuddy;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import lombok.NonNull;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

import java.awt.*;
import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.List;

import static jdplus.toolkit.desktop.plugin.tsproviders.TsProviderProperties.*;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(DataSourceProviderBuddy.class)
public final class SpreadsheetProviderBuddy implements DataSourceProviderBuddy {

    private static final String SOURCE = "XCLPRVDR";

    @Override
    public String getProviderName() {
        return SOURCE;
    }

    @Override
    public Image getIconOrNull(int type, boolean opened) {
        return ImageUtilities.loadImage("jdplus/spreadsheet/desktop/plugin/document-table.png", true);
    }

    @Override
    public Image getIconOrNull(DataSource dataSource, int type, boolean opened) {
        return ImageUtilities.loadImage("jdplus/spreadsheet/desktop/plugin/tables.png", true);
    }

    @Override
    public Image getIconOrNull(DataSet dataSet, int type, boolean opened) {
        switch (dataSet.getKind()) {
            case COLLECTION:
                return ImageUtilities.loadImage("jdplus/spreadsheet/desktop/plugin/table-sheet.png", true);
            case SERIES:
                return ImageUtilities.loadImage("jdplus/spreadsheet/desktop/plugin/table-select-row.png", true);
        }
        return null;
    }

    @Override
    public List<Sheet.Set> getSheetOfBeanOrNull(@NonNull Object bean) throws IntrospectionException {
        return bean instanceof SpreadSheetBean ? createSheetSets((SpreadSheetBean) bean) : null;
    }

    private List<Sheet.Set> createSheetSets(SpreadSheetBean bean) {
        NodePropertySetBuilder b = new NodePropertySetBuilder();
        return Arrays.asList(
                createSource(b, bean),
                createOptions(b, bean)
        );
    }

    @Messages({
        "bean.source.display=Source",
        "bean.source.description="
    })
    private Sheet.Set createSource(NodePropertySetBuilder b, SpreadSheetBean bean) {
        b.reset("source")
                .display(Bundle.bean_source_display())
                .description(Bundle.bean_source_description());
        TsManager.get()
                .getProvider(FileLoader.class, SOURCE)
                .ifPresent(o -> addFile(b, o, bean));
        return b.build();
    }

    @Messages({
        "bean.options.display=Options",
        "bean.options.description="
    })
    private Sheet.Set createOptions(NodePropertySetBuilder b, SpreadSheetBean bean) {
        b.reset("options")
                .display(Bundle.bean_options_display())
                .description(Bundle.bean_options_description());
        addObsFormat(b, bean::getFormat, bean::setFormat);
        addObsGathering(b, bean::getGathering, bean::setGathering);
        return b.build();
    }
}
