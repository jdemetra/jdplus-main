/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.tramoseats.base.core.tramo.internal;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class ArmaModuleTest {

    public ArmaModuleTest() {
    }

    @Test
    public void testProd() {
        ArmaModule test = ArmaModule.builder().build();
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(Doubles.of(Data.PROD)).arima(sarima).build();
        SarimaOrders nspec = test.process(regarima, true);
//        System.out.println("Prod");
//        System.out.println(nspec);
    }

    @Test
    public void testX() {
        ArmaModule test = ArmaModule.builder().build();
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(Doubles.of(Data.EXPORTS)).arima(sarima).build();
        SarimaOrders nspec = test.process(regarima, true);
//        System.out.println("X");
//        System.out.println(nspec);
    }

    @Test
    public void testProdLegacy() {

        ec.tstoolkit.modelling.arima.tramo.ArmaModule arma = new ec.tstoolkit.modelling.arima.tramo.ArmaModule();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
        ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
        desc.setAirline(true);
        context.description = desc;
        context.hasseas = true;
        arma.process(context);
//        System.out.println("Prod-Legacy");
//        System.out.println(context.description.getSpecification());
//        System.out.println(diff.getD());
//        System.out.println(diff.getBD());
//        System.out.println(diff.isMeanCorrection());
    }
    
    @Test
    public void testXLegacy() {

        ec.tstoolkit.modelling.arima.tramo.ArmaModule arma = new ec.tstoolkit.modelling.arima.tramo.ArmaModule();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1994, 0, Data.EXPORTS, true);
        ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
        ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
        desc.setAirline(true);
        context.description = desc;
        context.hasseas = true;
        arma.process(context);
//        System.out.println("X-Legacy");
//        System.out.println(context.description.getSpecification());
//        System.out.println(diff.getD());
//        System.out.println(diff.getBD());
//        System.out.println(diff.isMeanCorrection());
    }
}
