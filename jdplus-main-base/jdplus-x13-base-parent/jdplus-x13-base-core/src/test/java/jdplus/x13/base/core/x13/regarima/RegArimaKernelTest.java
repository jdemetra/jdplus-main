/*
 * Copyright 2019 National Bank of Belgium
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
package jdplus.x13.base.core.x13.regarima;

import jdplus.toolkit.base.api.data.AggregationType;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.x13.base.api.regarima.RegressionSpec;
import jdplus.x13.base.api.regarima.RegressionTestSpec;
import jdplus.x13.base.api.regarima.TradingDaysSpec;
import jdplus.toolkit.base.api.timeseries.StaticTsDataSupplier;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.calendars.Calendar;
import jdplus.toolkit.base.api.timeseries.calendars.EasterRelatedDay;
import jdplus.toolkit.base.api.timeseries.calendars.FixedDay;
import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class RegArimaKernelTest {

    private static final double[] data, datamissing;
    public static final Calendar france, belgium;
    public static final ec.tstoolkit.timeseries.calendars.NationalCalendar ofrance, obelgium;

    static {
        data = Data.PROD.clone();
        datamissing = Data.PROD.clone();
        datamissing[2] = Double.NaN;
        datamissing[100] = Double.NaN;
        datamissing[101] = Double.NaN;
        datamissing[102] = Double.NaN;
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new FixedDay(7, 14));
        holidays.add(new FixedDay(5, 8));
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.ARMISTICE);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.WHITMONDAY);

        france = new Calendar(holidays.toArray(new Holiday[holidays.size()]), true);

        ofrance = new ec.tstoolkit.timeseries.calendars.NationalCalendar();
        ofrance.add(new ec.tstoolkit.timeseries.calendars.FixedDay(13, Month.July));
        ofrance.add(new ec.tstoolkit.timeseries.calendars.FixedDay(7, Month.May));
        ofrance.add(new ec.tstoolkit.timeseries.calendars.FixedDay(10, Month.November));
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.AllSaintsDay);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.Assumption);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.Christmas);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.MayDay);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.NewYear);
        ofrance.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.Ascension);
        ofrance.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.EasterMonday);
        ofrance.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.PentecostMonday);

        holidays = new ArrayList<>();
        holidays.add(new FixedDay(7, 21));
        holidays.add(new FixedDay(1, 11));
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.WHITMONDAY);

        belgium = new Calendar(holidays.toArray(new Holiday[holidays.size()]), true);

        obelgium = new ec.tstoolkit.timeseries.calendars.NationalCalendar();
        obelgium.add(new ec.tstoolkit.timeseries.calendars.FixedDay(20, Month.July));
        obelgium.add(new ec.tstoolkit.timeseries.calendars.FixedDay(10, Month.January));
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.AllSaintsDay);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.Assumption);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.Christmas);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.MayDay);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.NewYear);
        obelgium.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.Ascension);
        obelgium.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.EasterMonday);
        obelgium.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.PentecostMonday);
    }

    public RegArimaKernelTest() {
    }

    @Test
    public void testProdMissing() {
        RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG5, null);
        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.ofInternal(start, datamissing);
        RegSarimaModel rslt = processor.process(s, null);
        System.out.println("New");
        System.out.println(rslt.getEstimation().getStatistics().getLogLikelihood());
    }

    @Test
    public void testProdLegacyMissing() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, datamissing, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy");
        System.out.println(rslt.estimation.getLikelihood().getLogLikelihood());
    }

    @Test
    public void testProd() {
        RegArimaSpec spec = RegArimaSpec.RG5;
//        OutlierSpec outlierSpec = spec.getOutliers().toBuilder()
//                .defaultCriticalValue(3)
//                .build();

//        spec = spec.toBuilder()
//                .outliers(outlierSpec)
//                .build();
        RegArimaKernel processor = RegArimaKernel.of(spec, null);
        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.ofInternal(start, data);
        RegSarimaModel rslt = processor.process(s, null);
        System.out.println(rslt.getData("residuals.df", Integer.class));
        RegArimaSpecification ospec = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.clone();
//        ospec.getOutliers().setDefaultCriticalValue(3);
        IPreprocessor oprocessor = ospec.build();
        ec.tstoolkit.timeseries.simplets.TsData os = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(os, null);
        assertEquals(rslt.getEstimation().getStatistics().getLogLikelihood(), orslt.estimation.getStatistics().logLikelihood, 1e-4);
    }

    @Test
    public void testInsee0() {
        TsData[] all = Data.insee();
        RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG0, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG0.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG0");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee1() {
        TsData[] all = Data.insee();
        RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG1, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG1.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG1");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee2() {
        TsData[] all = Data.insee();
        RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG2, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG2.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG2");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee3() {
        TsData[] all = Data.insee();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG3, null);
            IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG3.build();
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG3");
        System.out.println(n);
        assertTrue(n >= .9 * all.length);
    }

    @Test
    public void testInsee4() {
        TsData[] all = Data.insee();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG4, null);
            IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG4.build();
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG4");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee5() {
        TsData[] all = Data.insee();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG5, null);
            IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.build();
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG5");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testYearly() {
        TsData[] all = Data.insee();

        RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG0, null);
        for (int i = 0; i < all.length; ++i) {
            TsData s = all[i].aggregate(TsUnit.P1Y, AggregationType.Average, true);
            RegSarimaModel rslt = processor.process(s, null);
            assertTrue(rslt != null);
//            System.out.println(rslt.getEstimation().getParameters().getValues());
        }
        processor = RegArimaKernel.of(RegArimaSpec.RG3, null);
        for (int i = 0; i < all.length; ++i) {
            TsData s = all[i].aggregate(TsUnit.P1Y, AggregationType.Average, true);
            RegSarimaModel rslt = processor.process(s, null);
            assertTrue(rslt != null);
//            System.out.println(rslt.getEstimation().getParameters().getValues());
        }
        processor = RegArimaKernel.of(RegArimaSpec.RG4, null);
        for (int i = 0; i < all.length; ++i) {
            TsData s = all[i].aggregate(TsUnit.P1Y, AggregationType.Average, true);
            RegSarimaModel rslt = processor.process(s, null);
            assertTrue(rslt != null);
//            System.out.println(rslt.getEstimation().getParameters().getValues());
        }
    }

    @Test
    public void testInseeFullc() {
        TsData[] all = Data.insee();
        RegArimaSpec spec = RegArimaSpec.RG5;
        ModellingContext context = new ModellingContext();
        context.getCalendars().set("france", france);

        RegressionSpec regSpec = spec.getRegression();
        TradingDaysSpec tdSpec = TradingDaysSpec.holidays("france", TradingDaysType.TD7, LengthOfPeriodType.LeapYear, RegressionTestSpec.Remove, true);
        spec = spec.toBuilder()
                .regression(regSpec.toBuilder()
                        .tradingDays(tdSpec)
                        .build())
                .build();

        ec.tstoolkit.algorithm.ProcessingContext ocontext = new ec.tstoolkit.algorithm.ProcessingContext();
        ocontext.getGregorianCalendars().set("france", new ec.tstoolkit.timeseries.calendars.NationalCalendarProvider(ofrance));
        ec.tstoolkit.modelling.arima.x13.RegArimaSpecification ospec = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.clone();
        ospec.getRegression().getTradingDays().setHolidays("france");
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            IPreprocessor oprocessor = ospec.build(ocontext);
            RegArimaKernel processor = RegArimaKernel.of(spec, context);
            ospec.getRegression().getTradingDays().setHolidays("france");
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG5c");
        System.out.println(n);

// The old implementation was bugged. 
//        assertTrue(n > .6 * all.length);
    }

    @Test
    public void testUser() {

        Random rnd = new Random(0);
        double[] z = new double[1200];
        for (int i = 0; i < z.length; ++i) {
            z[i] = rnd.nextDouble() - .5;
        }
        TsData trnd = TsData.ofInternal(TsPeriod.monthly(1960, 1), z);
        ModellingContext context = new ModellingContext();
        TsDataSuppliers suppliers = new TsDataSuppliers();
        suppliers.set("test", new StaticTsDataSupplier(trnd));
        context.getTsVariableManagers().set("vars", suppliers);

        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.ofInternal(start, data);

        TsContextVariable tv = new TsContextVariable("vars.test");
        Variable<TsContextVariable> var = Variable.<TsContextVariable>builder()
                .core(tv)
                .name("test")
                .coefficients(null)
                .build();
        RegArimaSpec nspec = RegArimaSpec.RG5;
        RegressionSpec regSpec = nspec.getRegression();
        regSpec = regSpec.toBuilder()
                .userDefinedVariable(var)
                .build();

        nspec = nspec.toBuilder()
                .regression(regSpec)
                .build();

        RegArimaKernel processor = RegArimaKernel.of(nspec, context);
        RegSarimaModel rslt = processor.process(s, null);
        System.out.println(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());

        var = Variable.<TsContextVariable>builder()
                .core(tv)
                .name("test")
                .coefficients(new Parameter[]{Parameter.fixed(-0.005)})
                .build();
        nspec = RegArimaSpec.RG5;
        regSpec = nspec.getRegression();
        regSpec = regSpec.toBuilder()
                .userDefinedVariable(var)
                .build();

        nspec = nspec.toBuilder()
                .regression(regSpec)
                .build();

        processor = RegArimaKernel.of(nspec, context);
        rslt = processor.process(s, null);
        System.out.println(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());

    }

    public static void stressTestProd() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            RegArimaSpecification spec = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.clone();
            IPreprocessor processor = spec.build();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
            ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("legacy: " + (t1 - t0));
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            RegArimaSpec spec = RegArimaSpec.RG5;
            RegArimaKernel processor = RegArimaKernel.of(spec, null);
            TsPeriod start = TsPeriod.monthly(1967, 1);
            TsData s = TsData.ofInternal(start, data);
            RegSarimaModel rslt = processor.process(s, null);
        }
        t1 = System.currentTimeMillis();
        System.out.println("new: " + (t1 - t0));
    }

    public static void main(String[] args) {
        stressTestProd();
    }
}
