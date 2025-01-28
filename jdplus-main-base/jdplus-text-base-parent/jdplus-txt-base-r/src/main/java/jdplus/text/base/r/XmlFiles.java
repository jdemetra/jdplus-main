/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.text.base.r;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import jdplus.text.base.api.XmlBean;
import jdplus.text.base.api.XmlProvider;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class XmlFiles {

    private final XmlProvider PROVIDER = new XmlProvider();

    static XmlProvider currentProvider() {
        return (XmlProvider) TsFactory.getDefault().getProvider(XmlProvider.NAME).orElse(null);
    }

    public String changeFile(String id, String nfile, String ofile) {
        Optional<DataSet> set = PROVIDER.toDataSet(TsMoniker.of(XmlProvider.NAME, id));
        Optional<String> m = set.<String>map(d -> {
            XmlBean bean = PROVIDER.decodeBean(d.getDataSource());
            if (ofile.isBlank() || bean.getFile().getName().equals(ofile)) {
                bean.setFile(Path.of(nfile).toFile());
                DataSource src = PROVIDER.encodeBean(bean);
                DataSet nd = d.toBuilder()
                        .dataSource(src)
                        .build();
                return PROVIDER.toMoniker(nd).getId();
            } else {
                return id;
            }
        });
        return m.orElse(id);
    }

    public DataSource source(String file, String cs) {
        XmlBean bean = new XmlBean();
        bean.setFile(Path.of(file).toFile());
        if (!cs.isEmpty()) {
            Charset charset = Charset.forName(cs);
            bean.setCharset(charset);
        }
        return PROVIDER.encodeBean(bean);
    }

    public String[] sheets(DataSource source) throws Exception {
        XmlProvider currentProvider = currentProvider();
        if (currentProvider == null) {
            throw new Exception("XmlProvider is not available");
        }
        try {
            currentProvider.open(source);
            List<DataSet> sheets = currentProvider.children(source);
            return sheets.stream().map(s -> currentProvider.getDisplayNodeName(s)).toArray(String[]::new);
        } finally {
            currentProvider.close(source);
        }
    }

    public String[] series(DataSource source, int sheet) throws Exception {
        XmlProvider currentProvider = currentProvider();
        if (currentProvider == null) {
            throw new Exception("XmlProvider is not available");
        }
        try {
            currentProvider.open(source);
            List<DataSet> sheets = currentProvider.children(source);
            if (sheet > sheets.size()) {
                throw new IllegalArgumentException("Invalid sheet");
            }
            DataSet ds = sheets.get(sheet - 1);
            List<DataSet> all = currentProvider.children(ds);
            return all.stream().map(s -> currentProvider.getDisplayNodeName(s)).toArray(String[]::new);
        } finally {
            currentProvider.close(source);
        }
    }

    public Ts series(DataSource source, int sheet, int series, boolean fullName) throws Exception {
        XmlProvider currentProvider = currentProvider();
        if (currentProvider == null) {
            throw new Exception("XmlProvider is not available");
        }
        try {
            currentProvider.open(source);
            List<DataSet> sheets = currentProvider.children(source);
            if (sheet > sheets.size()) {
                throw new IllegalArgumentException("Invalid sheet");
            }
            DataSet ds = sheets.get(sheet - 1);
            List<DataSet> all = currentProvider.children(ds);
            if (series > all.size()) {
                throw new IllegalArgumentException("Invalid sheet");
            }
            DataSet s = all.get(series - 1);
            TsMoniker moniker = currentProvider.toMoniker(s);
            return currentProvider.getTs(moniker, TsInformationType.All).withName(fullName ? currentProvider.getDisplayName(s) : currentProvider.getDisplayNodeName(s));
        } finally {
            currentProvider.close(source);
        }
    }

    public TsCollection collection(DataSource source, int sheet, boolean fullNames) throws Exception {
        XmlProvider currentProvider = currentProvider();
        if (currentProvider == null) {
            throw new Exception("XmlProvider is not available");
        }
        try {
            currentProvider.open(source);
            List<DataSet> sheets = currentProvider.children(source);
            if (sheet > sheets.size()) {
                throw new IllegalArgumentException("Invalid sheet");
            }
            DataSet ds = sheets.get(sheet - 1);
            TsMoniker moniker = currentProvider.toMoniker(ds);
            if (fullNames) {
                return currentProvider.getTsCollection(moniker, TsInformationType.All).withName(currentProvider.getDisplayName(ds));
            } else {
                List<DataSet> schildren = currentProvider.children(ds);
                List<Ts> all = schildren.stream().map(c -> of(currentProvider, c)).toList();
                return TsCollection.builder()
                        .name(currentProvider.getDisplayName(ds))
                        .moniker(moniker)
                        .items(all)
                        .build();
            }
        } finally {
            currentProvider.close(source);
        }
    }

    private Ts of(XmlProvider provider, DataSet c) {
        TsMoniker moniker = provider.toMoniker(c);
        try {
            return provider.getTs(moniker, TsInformationType.All)
                    .withName(provider.getDisplayNodeName(c));
        } catch (IOException ex) {
            return Ts.builder()
                    .moniker(moniker)
                    .data(TsData.empty("Unavailable"))
                    .build();

        }
    }

    public void setPaths(String[] paths) throws Exception {
        XmlProvider provider = currentProvider();
        if (provider == null) {
            throw new Exception("XmlProvider is not available");
        }
        File[] files = Arrays.stream(paths).map(p -> Path.of(p).toFile()).toArray(File[]::new);
        provider.setPaths(files);
    }

    public DataSet decode(String id) {
        Optional<DataSet> set = PROVIDER.toDataSet(TsMoniker.of(XmlProvider.NAME, id));
        return set.orElse(null);
    }

    public String encode(DataSet set) {
        return PROVIDER.toMoniker(set).getId();
    }

    public XmlBean sourceOf(DataSet set) {
        return PROVIDER.decodeBean(set.getDataSource());
    }

    public DataSet seriesDataSet(DataSource source, int collectionIndex, int seriesIndex) {
        return DataSet.builder(source, DataSet.Kind.SERIES)
                .parameter("collectionIndex", Integer.toString(collectionIndex - 1))
                .parameter("seriesIndex", Integer.toString(seriesIndex - 1))
                .build();
    }

    public DataSet sheetDataSet(DataSource source, String sheetName) {
        return DataSet.builder(source, DataSet.Kind.COLLECTION)
                .parameter("sheetName", sheetName)
                .build();
    }
}
