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
package jdplus.sa.desktop.plugin.output.impl;

import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaOutputFactory;
import jdplus.sa.base.csv.CsvArrayOutputConfiguration;
import jdplus.sa.base.csv.CsvArrayOutputFactory;
import jdplus.sa.desktop.plugin.output.AbstractOutputNode;
import jdplus.sa.desktop.plugin.output.Arrays;
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
 * @author Philippe Charles
 */
@ServiceProvider(service = OutputFactoryBuddy.class, position = 1000)
public final class CsvArraysOutputBuddy implements OutputFactoryBuddy, Configurable, ConfigEditor, Resetable {

    private final BeanConfigurator<CsvArrayOutputConfiguration, CsvArraysOutputBuddy> configurator = createConfigurator();
    private CsvArrayOutputConfiguration config = new CsvArrayOutputConfiguration();

    public CsvArraysOutputBuddy() {
    }

    @Override
    public AbstractOutputNode createNode() {
        return new CsvArraysNode(config);
    }

    @Override
    public String getName() {
        return CsvArrayOutputFactory.NAME;
    }

    @Override
    public AbstractOutputNode createNodeFor(Object properties) {
        return properties instanceof CsvArrayOutputConfiguration ? new CsvArraysNode((CsvArrayOutputConfiguration) properties) : null;
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
        config = new CsvArrayOutputConfiguration();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static BeanConfigurator<CsvArrayOutputConfiguration, CsvArraysOutputBuddy> createConfigurator() {
        return new BeanConfigurator<>(new CsvArraysOutputBeanHandler(), new CsvArraysOutputConverter(), new CsvArraysOutputBeanEditor());
    }

    private static final class CsvArraysOutputBeanHandler implements BeanHandler<CsvArrayOutputConfiguration, CsvArraysOutputBuddy> {

        @Override
        public CsvArrayOutputConfiguration load(CsvArraysOutputBuddy resource) {
            return resource.config.clone();
        }

        @Override
        public void store(CsvArraysOutputBuddy resource, CsvArrayOutputConfiguration bean) {
            resource.config = bean;
        }
    }

    private static final class CsvArraysOutputBeanEditor implements BeanEditor {

        @Override
        public boolean editBean(Object bean) throws IntrospectionException {
            return new PropertySheetDialogBuilder()
                    .title("Edit csv arrays output config")
                    .editNode(new CsvArraysNode((CsvArrayOutputConfiguration) bean));
        }
    }

    private static final class CsvArraysOutputConverter implements Converter<CsvArrayOutputConfiguration, Config> {

        //        private final Property<CsvLayout> presentationParam = Property.of("presentation", CsvLayout.List, Parser.onEnum(CsvLayout.class), Formatter.onEnum());
        private final Property<File> folderParam = Property.of("folder", Path.of("").toFile(), Parser.onFile(), Formatter.onFile());
        private final Property<String> filePrefixParam = Property.of("filePrefix", "series", Parser.onString(), Formatter.onString());
        private final Property<String> arraysParam = Property.of("arrays", null, Parser.onString(), Formatter.onString());
        private final BooleanProperty fullNameParam = BooleanProperty.of("fullName", true);

        @Override
        public Config doForward(CsvArrayOutputConfiguration a) {
            Config.Builder result = Config.builder("outputs", "csv_array", "3.0");
//            presentationParam.set(result::parameter, a.getPresentation());
            folderParam.set(result::parameter, a.getFolder());
            filePrefixParam.set(result::parameter, a.getFilePrefix());
            arraysParam.set(result::parameter, a.getArrays().stream().collect(Collectors.joining(",")));
            fullNameParam.set(result::parameter, a.isFullName());
            return result.build();
        }

        @Override
        public CsvArrayOutputConfiguration doBackward(Config b) {
            CsvArrayOutputConfiguration result = new CsvArrayOutputConfiguration();
//            result.setPresentation(presentationParam.get(b::getParameter));
            result.setFolder(folderParam.get(b::getParameter));
            result.setFilePrefix(filePrefixParam.get(b::getParameter));
            result.setArrays(splitToStream(',', nullToEmpty(arraysParam.get(b::getParameter))).map(String::trim).toList());
            result.setFullName(fullNameParam.get(b::getParameter));
            return result;
        }
    }

    private final static class CsvArraysNode extends AbstractOutputNode<CsvArrayOutputConfiguration> {

        private static CsvArrayOutputConfiguration newConfiguration() {
            CsvArrayOutputConfiguration config = new CsvArrayOutputConfiguration();
            config.setArrays(OutputSelection.arraysItems(SaManager.processors()));
            return config;
        }

        public CsvArraysNode() {
            super(newConfiguration());
            setDisplayName(CsvArrayOutputFactory.NAME);
        }

        public CsvArraysNode(CsvArrayOutputConfiguration config) {
            super(config);
            setDisplayName(CsvArrayOutputFactory.NAME);
        }

        @Override
        protected Sheet createSheet() {
            CsvArrayOutputConfiguration config = getLookup().lookup(CsvArrayOutputConfiguration.class);

            Sheet sheet = super.createSheet();
            NodePropertySetBuilder builder = new NodePropertySetBuilder();

            builder.reset("Location");
            builder.withFile().select(config, "Folder").directories(true).description("Base output folder. Will be extended by the workspace and processing names").add();
            builder.with(String.class).select(config, "filePrefix").display("File Prefix").add();
            sheet.put(builder.build());

//            builder.reset("Layout");
//            builder.withEnum(CsvLayout.class).select(config, "Presentation").add();
            builder.withBoolean().select(config, "FullName").display("Full series name")
                    .description("If true, the fully qualified name of the series will be used. "
                            + "If false, only the name of the series will be displayed.").add();
            sheet.put(builder.build());

            builder.reset("Content");
            builder.with(List.class).select(config, "Arrays").editor(Arrays.class).add();
            sheet.put(builder.build());
            return sheet;
        }

        @Override
        public SaOutputFactory getFactory() {
            return new CsvArrayOutputFactory(getLookup().lookup(CsvArrayOutputConfiguration.class));
        }
    }
    //</editor-fold>
}
