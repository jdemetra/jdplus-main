/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.base.core.x11.extremevaluecorrector;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.stats.DescriptiveStatistics;
import jdplus.x13.base.api.x11.SeasonalFilterOption;
import jdplus.x13.base.core.x11.X11Context;
import jdplus.x13.base.core.x11.filter.X11SeasonalFilterProcessor;
import jdplus.x13.base.core.x11.filter.X11SeasonalFiltersFactory;

/**
 *
 * @author Christiane.Hofer@bundesbank.de
 */
public class CrossValidation {

    public CrossValidation() {
    }

    @lombok.Getter
    Map<String, String> result;

    X11Context context;

    @lombok.Getter
    SeasonalFilterOption seasonalFilterOptionCV;

    /**
     * Calculates the seasonal filters for the series s
     *
     * @param s series
     * @param ct context
     *
     */
    public void calculatedSF_CrossValidation(final DoubleSeq s, X11Context ct) {
        context = ct;
        DefaultExtremeValuesCorrector deEVC = new DefaultExtremeValuesCorrector();
        double[] one = new double[s.length()];
        int period = context.getPeriod();

        Arrays.fill(one, 1.0);

        deEVC.setSweights(DoubleSeq.of(one)); 
        DoubleSeq sqSeriesEVR = deEVC.computeCVCorrections(s, period);
        SeasonalFilterOption[] seasonalFilterOptions = context.getCvsfo();
        SeasonalFilterOption[] seasonalFilterOptions_1 = new SeasonalFilterOption[period];

        Map<SeasonalFilterOption, Double> rmse = new EnumMap<>(SeasonalFilterOption.class);
        for (SeasonalFilterOption seasonalFilterOption : seasonalFilterOptions) {
            for (int i = 0; i < period; i++) {
                seasonalFilterOptions_1[i] = seasonalFilterOption;
            }

            X11SeasonalFilterProcessor filter = X11SeasonalFiltersFactory.filter(period, seasonalFilterOptions_1);

            DoubleSeq sqFilteredSeries = s;
            //itterate from the first value of the Timeseries in anual Steps up to the lastValue of the time Series ignoring the values in the fcast and bcast horizont, they are only used for the calcualtion of the seasonally adjusted
            int nfc = context.getBackcastHorizon();
            int nbc = context.getForecastHorizon();
            int ny = s.length();// length of timeseries
            int iter = nfc;
            double[] distance = new double[ny - nfc - nbc];
            double[] SeriesEVR_itter = new double[ny];
            while (iter < ny - nfc - nbc) {
                s.copyTo(SeriesEVR_itter, 0);
                for (int i = iter; i < iter + period; i++) {
                    if (i < ny - nfc - nbc) {
                        SeriesEVR_itter[i] = sqSeriesEVR.get(i);
                    }
                }
                DoubleSeq sqFilteredSeriesEVR = filter.process(DoubleSeq.of(SeriesEVR_itter), 0);

                for (int i = iter; i < iter + period; i++) {
                    if (i < ny - nfc - nbc) {
                        distance[i] = sqFilteredSeries.get(i) - sqFilteredSeriesEVR.get(i);
                    }
                }
                iter += period;
            }

            DoubleSeq dSdistance = DoubleSeq.of(distance);

            DescriptiveStatistics dS = DescriptiveStatistics.of(dSdistance);
            rmse.put(seasonalFilterOption, dS.getRmse());
        }
        seasonalFilterOptionCV = Collections.min(rmse.entrySet(), Map.Entry.comparingByValue()).getKey();

        Map<String, String> res = new TreeMap<>();
        for (Map.Entry<SeasonalFilterOption, Double> entry : rmse.entrySet()) {
            SeasonalFilterOption sfo = entry.getKey();
            Double value = entry.getValue();
            res.put(sfo.name(), String.valueOf(value));
        }
        
        res.put(CV, seasonalFilterOptionCV.toString());
        res.put(CV_CRITERIA, context.getCvqc().toString());
        res.put(CV_FILTER_OPTIONS, Arrays.toString(context.getCvsfo()));
        result = res;
    }

    public static final String CV = "CrossValidation";
    public static final String CV_FILTER_OPTIONS = "CV Filter Options";
    public static final String CV_CRITERIA = "CV Criteria";

    public SeasonalFilterOption[] getSeasonalfilterOptionsCV() {

        SeasonalFilterOption[] cvFilters = new SeasonalFilterOption[context.getPeriod()];
        Arrays.fill(cvFilters, seasonalFilterOptionCV);
        return cvFilters;
    }

}
