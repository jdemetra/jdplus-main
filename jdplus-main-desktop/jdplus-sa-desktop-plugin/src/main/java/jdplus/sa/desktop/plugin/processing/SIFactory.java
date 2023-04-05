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
package jdplus.sa.desktop.plugin.processing;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.util.Id;
import java.util.function.Function;

/**
 *
 * @author PALATEJ
 * @param <D>
 */
public abstract class SIFactory<D extends TsDocument<?, ?>>
        extends ProcDocumentItemFactory<D, TsData[]> {

    protected SIFactory(Class<D> documentType, Id id, Function<D, SeriesDecomposition> extractor) {
        super(documentType, id, extractor.andThen((SeriesDecomposition source) -> {
            if (source == null)
                return null;
            TsData seas = source.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
            TsData i = source.getSeries(ComponentType.Irregular, ComponentInformation.Value);
            if (seas == null && i == null) {
                return null;
            }
            TsData si;
            DecompositionMode mode = source.getMode();
            if (mode.isMultiplicative()) {
                si = TsData.multiply(seas, i);
                if (seas == null) {
                    seas = TsData.of(i.getStart(), DoubleSeq.onMapping(i.length(), k -> 1));
                }
            } else {
                si = TsData.add(seas, i);
                if (seas == null) {
                    seas = TsData.of(i.getStart(), DoubleSeq.onMapping(i.length(), k -> 0));
                }
            }
            return new TsData[]{seas, si};
        }),
                 new SiRatioUI());
    }
}
