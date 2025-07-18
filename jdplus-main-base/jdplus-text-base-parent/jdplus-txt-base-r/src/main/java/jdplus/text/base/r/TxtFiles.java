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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import jdplus.text.base.api.TxtBean;
import jdplus.text.base.api.TxtProvider;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import jdplus.toolkit.base.tsp.util.PropertyHandler;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TxtFiles {

    private final TxtProvider PROVIDER = new TxtProvider();

    TxtProvider currentProvider() {
        return (TxtProvider) TsFactory.getDefault().getProvider(TxtProvider.NAME).orElse(null);
    }

    public String changeFile(String id, String nfile, String ofile) {
        Optional<DataSet> set = PROVIDER.toDataSet(TsMoniker.of(TxtProvider.NAME, id));
        Optional<String> m = set.<String>map(d -> {
            TxtBean bean = PROVIDER.decodeBean(d.getDataSource());
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

    public DataSource source(String file, ObsFormat obsFormat, ObsGathering obsGathering, String cs, String delimiter, String txtqualifier, boolean headers, int skiplines) {
        TxtBean bean = new TxtBean();
        bean.setFile(Path.of(file).toFile());
        if (cs != null && cs.length() > 0) {
            bean.setCharset(Charset.forName(cs));
        }
        if (delimiter != null) {
            bean.setDelimiter(TxtBean.Delimiter.valueOf(delimiter));
        }
        if (txtqualifier != null) {
            bean.setTextQualifier(TxtBean.TextQualifier.valueOf(txtqualifier));
        }
        if (obsFormat != null) {
            bean.setFormat(obsFormat);
        }
        if (obsGathering != null) {
            bean.setGathering(obsGathering);
        }
        bean.setHeaders(headers);
        bean.setSkipLines(skiplines);
        return PROVIDER.encodeBean(bean);
    }

    public String[] series(DataSource source) throws Exception {
        TxtProvider currentProvider = currentProvider();
        if (currentProvider == null) {
            throw new Exception("TxtProvider is not available");
        }
        try {
            currentProvider.open(source);
            List<DataSet> all = currentProvider.children(source);
            return all.stream().map(s -> currentProvider.getDisplayNodeName(s)).toArray(String[]::new);
        } finally {
            currentProvider.close(source);
        }
    }

    public String seriesIdentifier(DataSource source, int series) throws IllegalArgumentException, IOException {
        TxtProvider currentProvider = currentProvider();
        if (currentProvider == null) {
            throw new RuntimeException("TxtProvider is not available");
        }
        DataSet.Converter<Integer> seriesParam = PropertyHandler.onInteger("seriesIndex", -1).asDataSetConverter();
        DataSet.Builder builder = DataSet.builder(source, DataSet.Kind.SERIES);
        seriesParam.set(builder, series-1);
        return currentProvider.toMoniker(builder.build()).getId();
    }

    public Ts series(DataSource source, int series) throws IllegalArgumentException, IOException {
        TxtProvider currentProvider = currentProvider();
        if (currentProvider == null) {
            throw new RuntimeException("TxtProvider is not available");
        }
        try {
            currentProvider.open(source);
            List<DataSet> all = currentProvider.children(source);
            if (series > all.size()) {
                throw new IllegalArgumentException("Invalid sheet");
            }
            DataSet s = all.get(series - 1);
            TsMoniker moniker = currentProvider.toMoniker(s);
            return currentProvider.getTs(moniker, TsInformationType.All);
        } finally {
            currentProvider.close(source);
        }
    }

    public TsCollection collection(DataSource source) throws Exception {
        TxtProvider currentProvider = currentProvider();
        if (currentProvider == null) {
            throw new Exception("TxtProvider is not available");
        }
        try {
            currentProvider.open(source);
            TsMoniker moniker = currentProvider.toMoniker(source);
            return currentProvider.getTsCollection(moniker, TsInformationType.All);
        } finally {
            currentProvider.close(source);
        }
    }

    public void setPaths(String[] paths) throws Exception {
        TxtProvider provider = currentProvider();
        if (provider == null) {
            throw new Exception("TxtProvider is not available");
        }
        File[] files = Arrays.stream(paths).map(p -> Path.of(p).toFile()).toArray(File[]::new);
        provider.setPaths(files);
    }

    public DataSet decode(String id) {
        Optional<DataSet> set = PROVIDER.toDataSet(TsMoniker.of(TxtProvider.NAME, id));
        return set.orElse(null);
    }

    public String encode(DataSet set) {
        return PROVIDER.toMoniker(set).getId();
    }

    public TxtBean sourceOf(DataSet set) {
        return PROVIDER.decodeBean(set.getDataSource());
    }

    public DataSet sheetDataSet(DataSource source) {
        return DataSet.builder(source, DataSet.Kind.COLLECTION)
                .build();
    }

    public DataSet seriesDataSet(DataSource source, int series) {
        return DataSet.builder(source, DataSet.Kind.SERIES)
                .parameter("seriesIndex", Integer.toString(series - 1))
                .build();
    }
}
