/*
 * Copyright 2013 National Bank of Belgium
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
package jdplus.cruncher;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.information.formatters.BasicConfiguration;
import jdplus.sa.base.api.EstimationPolicy;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaDiagnosticsFactory;
import jdplus.sa.base.api.SaItem;
import jdplus.sa.base.api.SaItems;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaOutputFactory;
import jdplus.sa.base.csv.CsvInformationFormatter;
import jdplus.sa.base.csv.CsvMatrixOutputConfiguration;
import jdplus.sa.base.csv.CsvMatrixOutputFactory;
import jdplus.sa.base.csv.CsvOutputConfiguration;
import jdplus.sa.base.csv.CsvOutputFactory;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.tsp.FileLoader;
import jdplus.toolkit.base.api.util.Paths;
import jdplus.toolkit.base.workspace.WorkspaceItemDescriptor;
import jdplus.toolkit.base.workspace.WorkspaceUtility;
import jdplus.toolkit.base.workspace.file.FileWorkspace;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdplus.cruncher.batch.SaBatchInformation;
import jdplus.cruncher.batch.SaBatchProcessor;
import jdplus.cruncher.core.FileRepository;
import lombok.NonNull;
import picocli.CommandLine;

/**
 *
 * @author Kristof Bayens
 */
@lombok.extern.java.Log
public final class App {

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                File userDir = Path.of(System.getProperty("user.dir")).toFile();
                generateDefaultConfigFile(userDir);
            } else {
                Args config = ArgsDecoder2.decode(args);
                if (config != null) {
                    process(config.getWorkspace(), config.getConfig());
                }
            }
        } catch (IOException | IllegalArgumentException ex) {
            reportException(ex);
            System.exit(-1);
        } catch (CommandLine.ExecutionException ex) {
            reportException(ex.getCause());
            System.exit(-1);
        } catch (Exception err) {
            System.exit(-1);
        }
    }

    private static void reportException(Throwable ex) {
        log.log(Level.SEVERE, null, ex);
        System.err.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
    }

    static void generateDefaultConfigFile(@NonNull File userDir) throws IOException {
        WsaConfig config = WsaConfig.generateDefault();
        File configFile = userDir.toPath().resolve(WsaConfig.DEFAULT_FILE_NAME).toFile();
        WsaConfig.write(configFile, config);
    }

    private static List<SaOutputFactory> createOutputFactories(WsaConfig config) {
        List<SaOutputFactory> output = new ArrayList<>();
        output.add(new CsvMatrixOutputFactory(getCsvMatrixOutputConfiguration(config)));
        output.add(new CsvOutputFactory(getCsvOutputConfiguration(config)));
        return output;
    }

    static void process(@NonNull File workspace, @NonNull WsaConfig config) throws IllegalArgumentException, IOException {

        try ( FileWorkspace ws = FileWorkspace.open(workspace.toPath(),
                config.format.equalsIgnoreCase("JD2") ? DemetraVersion.JD2 : DemetraVersion.JD3)) {
            ModellingContext cxt = WorkspaceUtility.context(ws, config.refresh);
            loadResources();
            enableDiagnostics(config.Matrix);
            process(ws, cxt, config);
        }
    }

    private static void process(FileWorkspace ws, ModellingContext context, WsaConfig config) throws IOException {
        applyFilePaths(getFilePaths(config));

//        FileRepository.loadAllCalendars(ws, context);
//        Map<WorkspaceItemDescriptor, TsDataSuppliers> vars = FileRepository.loadAllVariables(ws, context);
        Map<WorkspaceItemDescriptor, SaItems> sa = FileRepository.loadAllSaProcessing(ws, context);

        if (sa.isEmpty()) {
            return;
        }
        int bundleSize = config.BundleSize == null ? 0 : config.BundleSize;

        applyOutputConfig(config, ws.getRootFolder());
        enableDiagnostics(config.Matrix);
        EstimationPolicy policy = new EstimationPolicy(config.getPolicy(), null);

        List<SaOutputFactory> output = createOutputFactories(config);
        TsInformationType type = config.refresh ? TsInformationType.Data : TsInformationType.None;
        for (Entry<WorkspaceItemDescriptor, SaItems> o : sa.entrySet()) {
            process(ws, o.getKey(), o.getValue(), context, output, bundleSize, policy, type);
        }
    }

    private static void process(FileWorkspace ws, WorkspaceItemDescriptor item, SaItems processing, ModellingContext context,
                                List<SaOutputFactory> output, int bundleSize, EstimationPolicy policy, TsInformationType type) throws IOException {

        if (type != TsInformationType.None) {
            System.out.println("Refreshing data");
        }
        List<SaItem> all = processing.getItems().stream().map(cur -> cur.refresh(policy, type)).collect(Collectors.toList());
        SaBatchInformation info = new SaBatchInformation(all.size() > bundleSize ? bundleSize : 0);
        info.setName(item.getKey().getId());
        info.setItems(all);
        SaBatchProcessor processor = new SaBatchProcessor(info, context, output, new ConsoleFeedback());
        processor.process();
        
        SaItems nprocessing = processing.toBuilder()
                .clearItems()
                .items(all)
                .build();

        System.out.println("Saving new processing...");
        FileRepository.storeSaProcessing(ws, item, nprocessing);

    }

    private static void loadResources() {
        loadFileProperties();
//        ServiceLoader.load(ITsProvider.class).forEach(TsFactory.instance::add);
//        ServiceLoader.load(ISaProcessingFactory.class).forEach(SaManager.instance::add);
//        ServiceLoader.load(ISaDiagnosticsFactory.class).forEach(SaManager.instance::add);
    }

    private static void loadFileProperties() {
        String basedir = System.getProperty("basedir");
        if (basedir != null) {
            Path file = java.nio.file.Path.of(basedir, "etc", "system.properties");
            try ( InputStream stream = Files.newInputStream(file)) {
                Properties properties = new Properties();
                properties.load(stream);
                System.getProperties().putAll(properties);
            } catch (IOException ex) {
                log.log(Level.WARNING, "While loading system properties", ex);
            }
        }
    }

    private static void applyFilePaths(File[] paths) {
        TsFactory.getDefault().getProviders().filter(f -> f instanceof FileLoader).forEach(o -> ((FileLoader) o).setPaths(paths));
    }

    private static void applyOutputConfig(WsaConfig config, Path rootFolder) {
        if (config.ndecs != null) {
            BasicConfiguration.setDecimalNumber(config.ndecs);
        }
        if (config.csvsep != null && config.csvsep.length() == 1) {
            CsvInformationFormatter.setCsvSeparator(config.csvsep.charAt(0));
        }

        if (config.Output == null) {
            config.Output = Paths.concatenate(rootFolder.toAbsolutePath().toString(), "Output");
        }
        File output = Path.of(config.Output).toFile();
        if (!output.exists()) {
            output.mkdirs();
        }

    }

    private static File[] getFilePaths(WsaConfig config) {
        return config.Paths != null
                ? Stream.of(config.Paths).map(pathname -> Path.of(pathname).toFile()).toArray(File[]::new)
                : new File[0];
    }

    private static CsvOutputConfiguration getCsvOutputConfiguration(WsaConfig config) {
        CsvOutputConfiguration result = new CsvOutputConfiguration();
        result.setFolder(Path.of(config.Output).toFile());
        result.setPresentation(config.getLayout());
        result.setSeries(Arrays.asList(config.TSMatrix));
        return result;
    }

    private static CsvMatrixOutputConfiguration getCsvMatrixOutputConfiguration(WsaConfig config) {
        CsvMatrixOutputConfiguration result = new CsvMatrixOutputConfiguration();
        result.setFolder(Path.of(config.Output).toFile());
        if (config.Matrix != null) {
            result.setItems(Arrays.asList(config.Matrix));
        }
        result.setShortColumnName(config.shortColumnHeaders);
        return result;
    }

    private static final String DIAGNOSTICS = "diagnostics";

    private static void enableDiagnostics(String[] items) {
        // step 1. We retrieve the used diagnostics
        Set<String> diags = new HashSet<>();
        if (items != null) {
            for (int i = 0; i < items.length; ++i) {
                if (InformationSet.isPrefix(items[i], DIAGNOSTICS)) {
                    int start = DIAGNOSTICS.length() + 1;
                    int end = items[i].indexOf(InformationSet.SEP, start);
                    if (end > 0) {
                        String diag = items[i].substring(start, end);
                        diags.add(diag);
                    }
                }
            }
        }
        SaManager.processors().forEach(
                processor -> {
                    List<SaDiagnosticsFactory> facs = processor.diagnosticFactories();
                    List<SaDiagnosticsFactory> nfacs = new ArrayList<>();
                    facs.forEach(dfac -> {
                        boolean active = dfac.isActive() || diags.contains(dfac.getName());
                        nfacs.add(dfac.activate(active));
                    });
                    processor.resetDiagnosticFactories(nfacs);
                }
        );

        // step 2. Enable/disables diag
//        SaManager.getDiagnostics().forEach(d -> d.setEnabled(diags.contains(d.getName().toLowerCase())));
    }

}
