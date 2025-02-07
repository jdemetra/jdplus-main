/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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
package jdplus.sa.desktop.plugin.output.impl;

import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaOutputFactory;
import jdplus.sa.base.csv.CsvMatrixOutputConfiguration;
import jdplus.sa.base.csv.CsvMatrixOutputFactory;
import jdplus.sa.desktop.plugin.output.AbstractOutputNode;
import jdplus.sa.desktop.plugin.output.Matrix;
import jdplus.sa.desktop.plugin.output.OutputFactoryBuddy;
import jdplus.sa.desktop.plugin.output.OutputSelection;
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
import java.util.stream.Collectors;

import static jdplus.toolkit.base.tsp.fixme.Strings.nullToEmpty;
import static jdplus.toolkit.base.tsp.fixme.Strings.splitToStream;

/**
 * @author Mats Maggi
 */
@ServiceProvider(service = OutputFactoryBuddy.class, position = 1100)
public class CsvMatrixOutputBuddy implements OutputFactoryBuddy, Configurable, ConfigEditor, Resetable {

    private static final BeanConfigurator<CsvMatrixOutputConfiguration, CsvMatrixOutputBuddy> configurator = createConfigurator();
    private CsvMatrixOutputConfiguration config = new CsvMatrixOutputConfiguration();

    public CsvMatrixOutputBuddy() {
    }

    @Override
    public String getName() {
        return CsvMatrixOutputFactory.NAME;
    }

    @Override
    public AbstractOutputNode createNode() {
        return new CsvMatrixNode(config);
    }

    @Override
    public AbstractOutputNode createNodeFor(Object properties) {
        return properties instanceof CsvMatrixOutputConfiguration ? new CsvMatrixNode((CsvMatrixOutputConfiguration) properties) : null;
    }

    @Override
    public @NonNull
    Config getConfig() {
        return configurator.getConfig(this);
    }

    @Override
    public void setConfig(@NonNull Config config) throws IllegalArgumentException {
        configurator.setConfig(this, config);
    }

    @Override
    public @NonNull
    Config editConfig(@NonNull Config config) throws IllegalArgumentException {
        return configurator.editConfig(config);
    }

    @Override
    public void configure() {
        Configurable.configure(this, this);
    }

    @Override
    public void reset() {
        config = new CsvMatrixOutputConfiguration();
    }

    private static BeanConfigurator<CsvMatrixOutputConfiguration, CsvMatrixOutputBuddy> createConfigurator() {
        return new BeanConfigurator<>(new CsvMatrixOutputBeanHandler(), new CsvMatrixOutputConverter(), new CsvMatrixOutputBeanEditor());
    }

    private static final class CsvMatrixOutputBeanHandler implements BeanHandler<CsvMatrixOutputConfiguration, CsvMatrixOutputBuddy> {

        @Override
        public CsvMatrixOutputConfiguration load(CsvMatrixOutputBuddy resource) {
            return resource.config.clone();
        }

        @Override
        public void store(CsvMatrixOutputBuddy resource, CsvMatrixOutputConfiguration bean) {
            resource.config = bean;
        }
    }

    private static final class CsvMatrixOutputBeanEditor implements BeanEditor {

        @Override
        public boolean editBean(Object bean) throws IntrospectionException {
            return new PropertySheetDialogBuilder()
                    .title("Edit Csv Matrix output config")
                    .editNode(new CsvMatrixNode((CsvMatrixOutputConfiguration) bean));
        }
    }

    private static final class CsvMatrixOutputConverter implements Converter<CsvMatrixOutputConfiguration, Config> {

        private final Property<File> folderParam = Property.of("folder", Path.of("").toFile(), Parser.onFile(), Formatter.onFile());
        private final Property<String> fileNameParam = Property.of("fileName", "series", Parser.onString(), Formatter.onString());
        private final Property<String> seriesParam = Property.of("items", "y,t,sa,s,i,ycal", Parser.onString(), Formatter.onString());
        private final BooleanProperty fullNameParam = BooleanProperty.of("fullName", true);
        private final BooleanProperty shortNameParam = BooleanProperty.of("shortName", true);

        @Override
        public Config doForward(CsvMatrixOutputConfiguration a) {
            Config.Builder result = Config.builder("outputs", "csv_matrix", "3.0");
            folderParam.set(result::parameter, a.getFolder());
            fileNameParam.set(result::parameter, a.getFileName());
            seriesParam.set(result::parameter, a.getItems().stream().collect(Collectors.joining(",")));
            fullNameParam.set(result::parameter, a.isFullName());
            shortNameParam.set(result::parameter, a.isShortColumnName());
            return result.build();
        }

        @Override
        public CsvMatrixOutputConfiguration doBackward(Config b) {
            CsvMatrixOutputConfiguration result = new CsvMatrixOutputConfiguration();
            result.setFolder(folderParam.get(b::getParameter));
            result.setFileName(fileNameParam.get(b::getParameter));
            result.setItems(splitToStream(",", nullToEmpty(seriesParam.get(b::getParameter))).map(String::trim).toList());
            result.setFullName(fullNameParam.get(b::getParameter));
            result.setShortColumnName(shortNameParam.get(b::getParameter));
            return result;
        }
    }

    public final static class CsvMatrixNode extends AbstractOutputNode<CsvMatrixOutputConfiguration> {

        private static CsvMatrixOutputConfiguration newConfiguration() {
            CsvMatrixOutputConfiguration config = new CsvMatrixOutputConfiguration();
            config.setItems(OutputSelection.matrixItems(SaManager.processors()));
            return config;
        }

        public CsvMatrixNode() {
            super(newConfiguration());
            setDisplayName(CsvMatrixOutputFactory.NAME);
        }

        public CsvMatrixNode(CsvMatrixOutputConfiguration config) {
            super(config);
            setDisplayName(CsvMatrixOutputFactory.NAME);
        }

        @Override
        protected Sheet createSheet() {
            CsvMatrixOutputConfiguration config = getLookup().lookup(CsvMatrixOutputConfiguration.class);
            Sheet sheet = super.createSheet();

            NodePropertySetBuilder builder = new NodePropertySetBuilder();
            builder.reset("Location");
            builder.withFile().select(config, "Folder").directories(true).description("Base output folder. Will be extended by the workspace and processing names").add();
            builder.with(String.class).select(config, "fileName").display("File Name").add();
            sheet.put(builder.build());

            builder.reset("Content");
            builder.with(List.class).select(config, "Items").editor(Matrix.class).add();
            sheet.put(builder.build());

            builder.reset("Layout");
            builder.withBoolean().select(config, "FullName").display("Full series name")
                    .description("If true, the fully qualified name of the series will be used. "
                            + "If false, only the name of the series will be displayed.").add();
            builder.withBoolean().select(config, "ShortColumnName").display("Short column headers")
                    .description("If false, full variable names will be displayed. "
                            + "If true, only short variable names will be displayed.").add();
            sheet.put(builder.build());

            return sheet;
        }

        @Override
        public SaOutputFactory getFactory() {
            return new CsvMatrixOutputFactory(getLookup().lookup(CsvMatrixOutputConfiguration.class));
        }
    }
}
