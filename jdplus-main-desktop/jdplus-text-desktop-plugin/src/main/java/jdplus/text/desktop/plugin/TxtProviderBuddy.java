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
package jdplus.text.desktop.plugin;

import jdplus.text.base.api.TxtBean;
import jdplus.toolkit.base.tsp.FileLoader;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceProviderBuddy;
import jdplus.toolkit.desktop.plugin.tsproviders.TsProviderProperties;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import lombok.NonNull;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

import java.awt.*;
import java.beans.IntrospectionException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(DataSourceProviderBuddy.class)
public final class TxtProviderBuddy implements DataSourceProviderBuddy {

    private static final String SOURCE = "Txt";

    @Override
    public String getProviderName() {
        return SOURCE;
    }

    @Override
    public Image getIconOrNull(int type, boolean opened) {
        return ImageUtilities.loadImage("jdplus/text/desktop/plugin/document-list.png", true);
    }

    @Override
    public List<Sheet.Set> getSheetOfBeanOrNull(@NonNull Object bean) throws IntrospectionException {
        return bean instanceof TxtBean ? createSheetSets((TxtBean) bean) : null;
    }

    private List<Sheet.Set> createSheetSets(TxtBean bean) {
        NodePropertySetBuilder b = new NodePropertySetBuilder();
        return Arrays.asList(
                createSource(b, bean),
                createContent(b, bean)
        );
    }

    private Sheet.Set createSource(NodePropertySetBuilder b, TxtBean bean) {
        b.reset("Source");
        TsManager.get()
                .getProvider(FileLoader.class, SOURCE)
                .ifPresent(o -> TsProviderProperties.addFile(b, o, bean));
        addReaderProperty(b, bean);
        addCsvDialectProperty(b, bean);
        return b.build();
    }

    private Sheet.Set createContent(NodePropertySetBuilder b, TxtBean bean) {
        b.reset("Content");
        TsProviderProperties.addObsFormat(b, bean::getFormat, bean::setFormat);
        TsProviderProperties.addObsGathering(b, bean::getGathering, bean::setGathering);
        return b.build();
    }

    private static void addReaderProperty(NodePropertySetBuilder b, TxtBean bean) {
        b.with(Charset.class)
                .select("charset", bean::getCharset, bean::setCharset)
                .display("Charset")
                .description("The charset used to read the file.")
                .add();

        b.withInt()
                .select("skipLines", bean::getSkipLines, bean::setSkipLines)
                .min(0)
                .display("Lines to skip")
                .description("The number of lines to skip before reading the data.")
                .add();
    }

    private static void addCsvDialectProperty(NodePropertySetBuilder b, TxtBean bean) {
        b.withEnum(TxtBean.Delimiter.class)
                .select("delimiter", bean::getDelimiter, bean::setDelimiter)
                .display("Delimiter")
                .description("The character used to separate fields.")
                .add();

        b.withEnum(TxtBean.TextQualifier.class)
                .select("textQualifier", bean::getTextQualifier, bean::setTextQualifier)
                .display("Text qualifier")
                .description("The characters used to retreive text fields.")
                .add();

        b.withBoolean()
                .select("headers", bean::isHeaders, bean::setHeaders)
                .display("Has headers?")
                .description("Use first line as headers.")
                .add();
    }
}
