/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.spreadsheet.desktop.plugin.sa;

import jdplus.sa.base.api.SaOutputFactory;
import jdplus.sa.desktop.plugin.output.AbstractOutputNode;
import jdplus.sa.desktop.plugin.output.OutputFactoryBuddy;
import jdplus.sa.desktop.plugin.output.Series;
import jdplus.spreadsheet.base.api.sa.SpreadsheetOutputConfiguration;
import jdplus.spreadsheet.base.api.sa.SpreadsheetOutputConfiguration.SpreadsheetLayout;
import jdplus.spreadsheet.base.api.sa.SpreadsheetOutputFactory;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.ConfigEditor;
import jdplus.toolkit.desktop.plugin.Converter;
import jdplus.toolkit.desktop.plugin.actions.Configurable;
import jdplus.toolkit.desktop.plugin.actions.Resetable;
import jdplus.toolkit.desktop.plugin.beans.BeanConfigurator;
import jdplus.toolkit.desktop.plugin.beans.BeanEditor;
import jdplus.toolkit.desktop.plugin.beans.BeanHandler;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.properties.PropertySheetDialogBuilder;
import lombok.NonNull;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.ServiceProvider;

import java.beans.IntrospectionException;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static jdplus.toolkit.base.tsp.fixme.Strings.nullToEmpty;
import static jdplus.toolkit.base.tsp.fixme.Strings.splitToStream;

/**
 * @author Philippe Charles
 */
@ServiceProvider(service = OutputFactoryBuddy.class, position = 1300)
public final class SpreadsheetOutputBuddy implements OutputFactoryBuddy, Configurable, ConfigEditor, Resetable {

    private final BeanConfigurator<SpreadsheetOutputConfiguration, SpreadsheetOutputBuddy> configurator = createConfigurator();
    private SpreadsheetOutputConfiguration config = new SpreadsheetOutputConfiguration();

    @Override
    public String getName() {
        return SpreadsheetOutputFactory.NAME;
    }

    @Override
    public AbstractOutputNode createNode() {
        return new SpreadsheetNode(config);
    }

    @Override
    public AbstractOutputNode createNodeFor(Object properties) {
        return properties instanceof SpreadsheetOutputConfiguration ? new SpreadsheetNode((SpreadsheetOutputConfiguration) properties) : null;
    }

    @Override
    public @NonNull Config getConfig() {
        return configurator.getConfig(this);
    }

    @Override
    public void setConfig(@NonNull Config config) throws IllegalArgumentException {
        configurator.setConfig(this, config);
    }

    @Override
    public @NonNull Config editConfig(@NonNull Config config) throws IllegalArgumentException {
        return configurator.editConfig(config);
    }

    @Override
    public void configure() {
        Configurable.configure(this, this);
    }

    @Override
    public void reset() {
        config = new SpreadsheetOutputConfiguration();
    }

    private static BeanConfigurator<SpreadsheetOutputConfiguration, SpreadsheetOutputBuddy> createConfigurator() {
        return new BeanConfigurator<>(new SpreadsheetOutputBeanHandler(), new SpreadsheetOutputConverter(), new SpreadsheetOutputBeanEditor());
    }

    private static final class SpreadsheetOutputBeanHandler implements BeanHandler<SpreadsheetOutputConfiguration, SpreadsheetOutputBuddy> {

        @Override
        public SpreadsheetOutputConfiguration load(SpreadsheetOutputBuddy resource) {
            return resource.config.clone();
        }

        @Override
        public void store(SpreadsheetOutputBuddy resource, SpreadsheetOutputConfiguration bean) {
            resource.config = bean;
        }
    }

    private static final class SpreadsheetOutputBeanEditor implements BeanEditor {

        @Override
        public boolean editBean(Object bean) throws IntrospectionException {
            return new PropertySheetDialogBuilder()
                    .title("Edit spreadsheet output config")
                    .editNode(new SpreadsheetNode((SpreadsheetOutputConfiguration) bean));
        }
    }

    private static final class SpreadsheetOutputConverter implements Converter<SpreadsheetOutputConfiguration, Config> {

        private final BooleanProperty saveModelParam = BooleanProperty.of("saveModel", false);
        private final BooleanProperty verticalOrientationParam = BooleanProperty.of("verticalOrientation", true);
        private final Property<SpreadsheetLayout> layoutParam = Property.of("layout", SpreadsheetLayout.BySeries, Parser.onEnum(SpreadsheetLayout.class), Formatter.onEnum());
        private final Property<File> folderParam = Property.of("folder", Path.of("").toFile(), Parser.onFile(), Formatter.onFile());
        private final Property<String> fileNameParam = Property.of("fileName", "series", Parser.onString(), Formatter.onString());
        private final Property<String> seriesParam = Property.of("series", "y,t,sa,s,i,ycal", Parser.onString(), Formatter.onString());
        private final BooleanProperty fullNameParam = BooleanProperty.of("fullName", true);

        @Override
        public Config doForward(SpreadsheetOutputConfiguration a) {
            Config.Builder result = Config.builder("outputs", "csv", "3.0");
            saveModelParam.set(result::parameter, a.isSaveModel());
            verticalOrientationParam.set(result::parameter, a.isVerticalOrientation());
            layoutParam.set(result::parameter, a.getLayout());
            folderParam.set(result::parameter, a.getFolder());
            fileNameParam.set(result::parameter, a.getFileName());
            seriesParam.set(result::parameter, String.join(",", a.getSeries()));
            fullNameParam.set(result::parameter, a.isFullName());
            return result.build();
        }

        @Override
        public SpreadsheetOutputConfiguration doBackward(Config b) {
            SpreadsheetOutputConfiguration result = new SpreadsheetOutputConfiguration();
            result.setSaveModel(saveModelParam.get(b::getParameter));
            result.setVerticalOrientation(verticalOrientationParam.get(b::getParameter));
            result.setLayout(layoutParam.get(b::getParameter));
            result.setFolder(folderParam.get(b::getParameter));
            result.setFileName(fileNameParam.get(b::getParameter));
            result.setSeries(splitToStream(',', nullToEmpty(seriesParam.get(b::getParameter))).map(String::trim).toList());
            result.setFullName(fullNameParam.get(b::getParameter));
            return result;
        }
    }

    private final static class SpreadsheetNode extends AbstractOutputNode<SpreadsheetOutputConfiguration> {

        public SpreadsheetNode(SpreadsheetOutputConfiguration config) {
            super(config);
            setDisplayName(SpreadsheetOutputFactory.NAME);
        }

        @Override
        protected Sheet createSheet() {
            SpreadsheetOutputConfiguration config = getLookup().lookup(SpreadsheetOutputConfiguration.class);

            Sheet sheet = super.createSheet();

            NodePropertySetBuilder b = new NodePropertySetBuilder();

            b.reset("Location");
            b.withFile()
                    .select("folder", config::getFolder, config::setFolder)
                    .directories(true)
                    .display("Folder")
                    .description("Base output folder. Will be extended by the workspace and processing names")
                    .add();
            b.with(String.class)
                    .select("FileName", config::getFileName, fixFileNameExtension(config::setFileName))
                    .display("File name")
                    .add();
            sheet.put(b.build());

            b.reset("Layout");
            b.withEnum(SpreadsheetLayout.class)
                    .select("layout", config::getLayout, config::setLayout)
                    .display("Layout")
                    .add();
            b.withBoolean()
                    .select("verticalOrientation", config::isVerticalOrientation, config::setVerticalOrientation)
                    .display("Vertical orientation")
                    .add();
            b.withBoolean()
                    .select("fullName", config::isFullName, config::setFullName)
                    .display("Full series name")
                    .description("If true, the fully qualified name of the series will be used (workbook + sheet + name). "
                            + "If false, only the name of the series will be displayed.")
                    .add();
            sheet.put(b.build());

            b.reset("Content");
            b.with(List.class)
                    .select("Series", config::getSeries, config::setSeries)
                    .editor(Series.class)
                    .add();
            sheet.put(b.build());

            return sheet;
        }

        @Override
        public SaOutputFactory getFactory() {
            return new SpreadsheetOutputFactory(getLookup().lookup(SpreadsheetOutputConfiguration.class));
        }

        private Consumer<String> fixFileNameExtension(Consumer<String> setter) {
            return fileName -> setter.accept(fixFileNameExtension(fileName));
        }

        private String fixFileNameExtension(String fileName) {
            return fileName.lastIndexOf('.') == -1 ? (fileName + ".xlsx") : fileName;
        }
    }
}
