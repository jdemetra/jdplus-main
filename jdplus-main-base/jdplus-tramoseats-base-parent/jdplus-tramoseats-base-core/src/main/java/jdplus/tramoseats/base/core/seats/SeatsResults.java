/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.tramoseats.base.core.seats;

import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.sa.base.api.ComponentDescriptor;
import jdplus.toolkit.base.api.util.Arrays2;
import java.util.List;
import jdplus.sa.base.api.ComponentType;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.core.arima.ArimaModel;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class SeatsResults implements GenericExplorable {

    public static final ComponentDescriptor SeasonallyAdjusted = new ComponentDescriptor("sa", 1, false, true);
    public static final ComponentDescriptor Trend = new ComponentDescriptor("trend", 0, true, true);
    public static final ComponentDescriptor Seasonal = new ComponentDescriptor("seasonal", 1, true, false);
    public static final ComponentDescriptor Transitory = new ComponentDescriptor("transitory", 2, true, false);
    public static final ComponentDescriptor Irregular = new ComponentDescriptor("irregular", 3, true, false);
    public static final List<ComponentDescriptor> descriptors = Arrays2.unmodifiableList(
            SeasonallyAdjusted,
            Trend,
            Seasonal,
            Transitory,
            Irregular);
    private static final ComponentDescriptor aIrregular = new ComponentDescriptor("irregular", 2, true, false);
    public static final List<ComponentDescriptor> airlineDescriptors = Arrays2.unmodifiableList(
            SeasonallyAdjusted,
            Trend,
            Seasonal,
            aIrregular);

    public static String[] getComponentsName(UcarimaModel ucm) {
        List<ComponentDescriptor> d = ucm.getComponentsCount() == 4
                ? descriptors : airlineDescriptors;
        String[] names = new String[d.size()];
        for (int i = 0; i < names.length; ++i) {
            names[i] = d.get(i).getName();
        }
        return names;
    }
    
    public static ArimaModel[] getComponents(UcarimaModel ucm) {
        List<ComponentDescriptor> d = ucm.getComponentsCount() == 4
                ? descriptors : airlineDescriptors;
        ArimaModel[] cmps = new ArimaModel[d.size()];
        for (int i = 0; i < cmps.length; ++i) {
            cmps[i] = d.get(i).isSignal() ? ucm.getComponent(d.get(i).getComponent()) : ucm.getComplement(d.get(i).getComponent());
        }
        return cmps;
    }

    public TsDomain getBackcastDomain() {
        TsData sa = initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
        return sa == null ? TsDomain.DEFAULT_EMPTY : sa.getDomain();
    }

    public TsDomain getForecastDomain() {
        TsData sa = initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        return sa == null ? TsDomain.DEFAULT_EMPTY : sa.getDomain();
    }

    public TsDomain getActualDomain() {
        TsData sa = initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
        return sa == null ? TsDomain.DEFAULT_EMPTY : sa.getDomain();
    }
    
    private SarimaModel originalModel;
    private SarimaModel finalModel;
    private double innovationVariance;
    private boolean meanCorrection;
    private boolean parametersCutOff, modelChanged;
    private UcarimaModel ucarimaModel, compactUcarimaModel;
    private SeriesDecomposition initialComponents, finalComponents;
}
