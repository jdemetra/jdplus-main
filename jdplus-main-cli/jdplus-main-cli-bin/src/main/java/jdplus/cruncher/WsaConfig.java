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

import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaDiagnosticsFactory;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.sa.base.csv.CsvInformationFormatter;
import jdplus.sa.base.csv.CsvLayout;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsDictionaries;
import jdplus.x13.base.api.x13.X13Dictionaries;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import nbbrd.io.WrappedIOException;
import nbbrd.io.xml.bind.Jaxb;
import lombok.NonNull;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = "wsaConfig")
public class WsaConfig {

    @XmlElement(name = "policy")
    public String policy = "parameters";
    @XmlElement(name = "refreshall")
    public Boolean refresh = true;
    @XmlElement(name = "output")
    public String Output;
    @XmlElementWrapper(name = "matrix")
    @XmlElement(name = "item")
    public String[] Matrix = new String[0];
    @XmlElementWrapper(name = "tsmatrix")
    @XmlElement(name = "series")
    public String[] TSMatrix = new String[0];
    @XmlElementWrapper(name = "paths")
    @XmlElement(name = "path")
    public String[] Paths;
    @XmlAttribute(name = "bundle")
    public Integer BundleSize = 10000;
    @XmlAttribute(name = "csvlayout")
    public String layout = "list";
    @XmlAttribute(name = "csvseparator")
    public String csvsep = String.valueOf(CsvInformationFormatter.getCsvSeparator());
    @XmlAttribute(name = "ndecs")
    public Integer ndecs = 6;
    @XmlAttribute(name = "fullseriesname")
    public Boolean fullSeriesName = true;
    @XmlAttribute(name = "shortcolumnheaders")
    public Boolean shortColumnHeaders = true;
    @XmlAttribute(name = "rsltnamelevel")
    public Integer resultNameLevel = 2;
    @XmlAttribute(name = "format")
    public String format = "JD3";

    public WsaConfig() {
    }

    public EstimationPolicyType getPolicy() {
        if (policy == null) {
            return EstimationPolicyType.None;
        } else if (policy.equalsIgnoreCase("n")
                || policy.equalsIgnoreCase("current")) {
            return EstimationPolicyType.Current;
        } else if (policy.equalsIgnoreCase("f")
                || policy.equalsIgnoreCase("fixed")) {
            return EstimationPolicyType.Fixed;
        } else if (policy.equalsIgnoreCase("fp")
                || policy.equalsIgnoreCase("fixedparameters")) {
            return EstimationPolicyType.FixedParameters;
        } else if (policy.equalsIgnoreCase("farp")
                || policy.equalsIgnoreCase("fixedarparameters")) {
            return EstimationPolicyType.FixedAutoRegressiveParameters;
        } else if (policy.equalsIgnoreCase("p")
                || policy.equalsIgnoreCase("parameters")) {
            return EstimationPolicyType.FreeParameters;
        } else if (policy.equalsIgnoreCase("c")
                || policy.equalsIgnoreCase("complete") || policy.equalsIgnoreCase("concurrent")) {
            return EstimationPolicyType.Complete;
        } else if (policy.equalsIgnoreCase("o")
                || policy.equalsIgnoreCase("outliers")) {
            return EstimationPolicyType.Outliers;
        } else if (policy.equalsIgnoreCase("l")
                || policy.equalsIgnoreCase("lastoutliers")) {
            return EstimationPolicyType.LastOutliers;
        } else if (policy.equalsIgnoreCase("stochastic")
                || policy.equalsIgnoreCase("s")) {
            return EstimationPolicyType.Outliers_StochasticComponent;
        } else {
            return EstimationPolicyType.None;
        }
    }

    public CsvLayout getLayout() {
        if (layout == null) {
            return CsvLayout.List;
        } else if (layout.equalsIgnoreCase("h")
                || layout.equalsIgnoreCase("htable")) {
            return CsvLayout.HTable;
        } else if (layout.equalsIgnoreCase("v")
                || layout.equalsIgnoreCase("vtable")) {
            return CsvLayout.VTable;
        } else {
            return CsvLayout.List;
        }
    }

    static WsaConfig read(File file) throws IOException {
        try {
            return Jaxb.Parser.of(WsaConfig.class).parseFile(file);
        } catch (WrappedIOException ex) {
            throw new IOException("Failed to parse config file '" + file + "'", ex.getCause());
        }
    }

    static void write(File file, WsaConfig config) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(WsaConfig.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(config, file);
        } catch (JAXBException ex) {
            throw new IOException("Failed to write config file '" + file + "'", unwrap(ex));
        }
    }

    private static Throwable unwrap(JAXBException ex) {
        return ex.getMessage() == null || ex.getMessage().isEmpty() ? ex.getCause() : ex;
    }

    static final String DEFAULT_FILE_NAME = "wsacruncher.params";

    @NonNull
    static WsaConfig generateDefault() {
        WsaConfig result = new WsaConfig();
        loadAll();
        List<String> mlist = new ArrayList<>();
        List<String> tlist = new ArrayList<>();

        Map<String, Class> dic = new LinkedHashMap<>();
        // for TramoSeats
        Dictionary tsdic = TramoSeatsDictionaries.TRAMOSEATSDICTIONARY;
        tsdic.entries().forEachOrdered(entry -> dic.put(entry.fullName(), entry.getOutputClass()));
//        // for X13
        Dictionary x13dic = X13Dictionaries.X13DICTIONARY;
        x13dic.entries().forEachOrdered(entry -> dic.put(entry.fullName(), entry.getOutputClass()));
        // series
        Set<Type> types = CsvInformationFormatter.formattedTypes();
        dic.entrySet().forEach(entry -> {
            if (entry.getValue() == TsData.class) {
                tlist.add(entry.getKey());
            } else if (types.contains(entry.getValue())) {
                mlist.add(entry.getKey());
            }
        });
        result.TSMatrix = tlist.toArray(result.TSMatrix);
        result.Matrix = mlist.toArray(result.Matrix);
        return result;
    }

    // use all diagnostics
    private static void loadAll() {
        List<SaProcessingFactory> processors = SaManager.processors();
        processors.forEach(fac -> {
            List<SaDiagnosticsFactory> diagnostics = fac.diagnosticFactories();
            List<SaDiagnosticsFactory> ndiagnostics = new ArrayList<>();
            diagnostics.forEach(dfac -> {
                ndiagnostics.add(dfac.activate(true));
            });
            fac.resetDiagnosticFactories(ndiagnostics);
        });
    }
}
