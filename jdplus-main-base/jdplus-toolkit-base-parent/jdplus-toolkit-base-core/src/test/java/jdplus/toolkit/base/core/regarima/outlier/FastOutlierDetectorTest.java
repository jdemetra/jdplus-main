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
package jdplus.toolkit.base.core.regarima.outlier;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.core.modelling.regression.AdditiveOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.LevelShiftFactory;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Random;
import java.util.function.DoubleSupplier;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.GenericTradingDaysFactory;
import jdplus.toolkit.base.core.modelling.regression.IOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.PeriodicOutlierFactory;

/**
 *
 * @author Jean Palate
 */
public class FastOutlierDetectorTest {
    
   

    public FastOutlierDetectorTest() {
    }

    //@Test
    public void testNew() {
        DataBlock rnd = DataBlock.make(600);
        Random gen = new Random(0);
        rnd.set((DoubleSupplier)gen::nextDouble);
        FastOutlierDetector sod = new FastOutlierDetector(null);
        sod.setOutlierFactories(AdditiveOutlierFactory.FACTORY, LevelShiftFactory.FACTORY_ZEROENDED);
        SarmaOrders spec = new SarmaOrders(12);
        spec.setBq(1);
        spec.setQ(1);
        SarimaModel model = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder()
                .y(rnd)
                .meanCorrection(true)
                .arima(model)
                .build();
            sod.process(regarima);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            sod.process(regarima);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    //@Test
    public void testLegacy() {
        ec.tstoolkit.data.DataBlock rnd = new ec.tstoolkit.data.DataBlock(600);
        Random gen = new Random(0);
        rnd.set((DoubleSupplier)gen::nextDouble);
        ec.tstoolkit.modelling.arima.tramo.SingleOutlierDetector sod=new ec.tstoolkit.modelling.arima.tramo.SingleOutlierDetector();
        ec.tstoolkit.sarima.SarmaSpecification spec=new ec.tstoolkit.sarima.SarmaSpecification(12);
        sod.addOutlierFactory(new ec.tstoolkit.timeseries.regression.AdditiveOutlierFactory());
        sod.addOutlierFactory(new ec.tstoolkit.timeseries.regression.LevelShiftFactory());
        sod.prepare(new ec.tstoolkit.timeseries.simplets.TsDomain(TsFrequency.Monthly, 1980, 0, 600), null);
        spec.setQ(1);
        spec.setBQ(1);
        ec.tstoolkit.sarima.SarimaModel model=new ec.tstoolkit.sarima.SarimaModel(spec);
        model.setDefault();
        System.out.println("Legacy");
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            sod.process(model, rnd);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }

        public static void main(String[] args) {
        stressTest();
    }

    public static void stressTest() {
        int K = 10000;
        double[] A = Data.PROD.clone();
        A[14] *= 1.3;
        A[55] *= .7;
        DoubleSeq Y = DoubleSeq.of(A);
        FastMatrix days = FastMatrix.make(A.length, 7);
        GenericTradingDaysFactory.fillTradingDaysMatrix(TsPeriod.monthly(1967, 1), false, days);
        FastMatrix td = GenericTradingDaysFactory.generateContrasts(DayClustering.TD3, days);

        int[] length = new int[]{40, 60, 120, 180, 240, 300};
        for (int l = 0; l<length.length; ++l) {
            long t0 = System.currentTimeMillis();
            for (int k = 0; k < K; ++k) {
                SarimaOrders spec=SarimaOrders.airline(12);
                SarimaModel model = SarimaModel.builder(spec)
                        .setDefault().build();
                forwardstep(model, Y.log().range(0, length[l]), td.extract(0, length[l], 0, td.getColumnsCount()));
//                OutliersDetection od = OutliersDetection.builder()
//                        .bsm(spec)
//                        .maxIter(1)
//                        .build();
//                od.process(Y.log().range(0, length[l]), td.extract(0, length[l], 0, td.getColumnsCount()), 12);
            }
            long t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
        }
    }
    
    private static boolean forwardstep(SarimaModel model, DoubleSeq y, FastMatrix W) {
        
        FastOutlierDetector sod=new FastOutlierDetector(null);
        IOutlierFactory[] factories=new IOutlierFactory[]{AdditiveOutlierFactory.FACTORY,LevelShiftFactory.FACTORY_ZEROENDED, new PeriodicOutlierFactory(12, true)};
        sod.setOutlierFactories(factories);
        sod.prepare(y.length());
        RegArimaModel<SarimaModel> regarima=RegArimaModel.builder()
                .arima(model)
                .y(y)
//                .addX(W)
                .build();
        sod.process(regarima);
        return true;
    }

}
