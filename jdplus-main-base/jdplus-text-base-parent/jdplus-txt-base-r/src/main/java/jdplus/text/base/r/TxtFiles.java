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
import java.nio.charset.Charset;
import java.util.ArrayList;
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

    public List<TsMoniker> changeFiles(List<TsMoniker> monikers, File ofile, File nfile) {
        List<TsMoniker> rslt = new ArrayList<>();
        for (TsMoniker moniker : monikers) {
            Optional<DataSet> set = PROVIDER.toDataSet(moniker);
            set.ifPresentOrElse(
                    (DataSet d) -> {
                        TxtBean bean = PROVIDER.decodeBean(d.getDataSource());
                        if (bean.getFile().equals(ofile)) {
                            bean.setFile(nfile);
                            DataSource src = PROVIDER.encodeBean(bean);

                            DataSet nd = d.toBuilder()
                                    .dataSource(src)
                                    .build();
                            rslt.add(PROVIDER.toMoniker(nd));
                        } else {
                            rslt.add(moniker);
                        }
                    }, () -> rslt.add(moniker));
        }
        return rslt;
    }

    public TsMoniker changeFile(TsMoniker moniker, File ofile, File nfile) {
        Optional<DataSet> set = PROVIDER.toDataSet(moniker);
        Optional<TsMoniker> m = set.<TsMoniker>map(d -> {
            TxtBean bean = PROVIDER.decodeBean(d.getDataSource());
            if (bean.getFile().equals(ofile)) {
                bean.setFile(nfile);
                DataSource src = PROVIDER.encodeBean(bean);

                DataSet nd = d.toBuilder()
                        .dataSource(src)
                        .build();
                return PROVIDER.toMoniker(nd);
            } else {
                return moniker;
            }
        });
        return m.orElse(moniker);
    }

    public DataSource source(String file, ObsFormat obsFormat, ObsGathering obsGathering, String cs, String delimiter, String txtqualifier, boolean headers, int skiplines) {
        TxtBean bean = new TxtBean();
        bean.setFile(new File(file));
        if (cs != null && cs.length()>0) {
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

    public Ts series(DataSource source, int series) throws Exception {
        TxtProvider currentProvider = currentProvider();
        if (currentProvider == null) {
            throw new Exception("TxtProvider is not available");
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
        File[] files = Arrays.stream(paths).map(p -> new File(p)).toArray(n -> new File[n]);
        provider.setPaths(files);
    }

}
