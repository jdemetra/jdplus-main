/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.arima;

import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.toolkit.base.core.ssf.arima.ExactArimaForecasts;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.core.ssf.arima.FastArimaForecasts;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class ArimaForecasts {

    @lombok.Value
    @lombok.Builder
    public static class Results implements GenericExplorable {

        RegArimaModel<SarimaModel> regarima;
        DoubleSeq forecasts;
        DoubleSeq forecastsErrors;
        DoubleSeq backcasts;
        DoubleSeq backcastsErrors;

        private static final String FCASTS = "forecasts", EFCASTS = "forecasts.se",
                BCASTS = "backcasts", EBCASTS = "backcasts.se";
        private static final InformationMapping<Results> MAPPING = new InformationMapping<Results>() {
            @Override
            public Class getSourceClass() {
                return Results.class;
             }
        };

        static {
            MAPPING.set(FCASTS, double[].class, source -> source.forecasts != null ? source.forecasts.toArray() : null);
            MAPPING.set(BCASTS, double[].class, source -> source.backcasts != null ? source.backcasts.toArray() : null);
            MAPPING.set(EFCASTS, double[].class, source -> source.forecastsErrors != null ? source.forecastsErrors.toArray() : null);
            MAPPING.set(EBCASTS, double[].class, source -> source.backcastsErrors != null ? source.backcastsErrors.toArray() : null);
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }
    }

    public Results process(RegArimaModel regarima, double mean, int nf, int nb, String method) {
        if (!method.equalsIgnoreCase("all")) {
            Results.Builder builder = Results.builder();
            boolean exact = method.equalsIgnoreCase("exact");
            jdplus.toolkit.base.core.arima.estimation.ArimaForecasts fcasts = exact ? new ExactArimaForecasts() : new FastArimaForecasts();
            if (exact) {
                fcasts.prepare(regarima.arima(), mean != 0);
            } else {
                fcasts.prepare(regarima.arima(), mean);
            }
            if (nf > 0) {
                builder.forecasts(fcasts.forecasts(regarima.getY(), nf));
            }
            if (nb > 0) {
                builder.backcasts(fcasts.backcasts(regarima.getY(), nb));
            }
            return builder.build();
        } else {
            return ssfcompute(regarima, nf, nb);
        }
    }

    private Results ssfcompute(RegArimaModel regarima, int nf, int nb) {
        StateComponent arima = SsfArima.stateComponent(regarima.arima());
        DoubleSeq y = regarima.getY();
        double[] yc = new double[y.length() + nf + nb];
        for (int i = 0; i < nb; ++i) {
            yc[i] = Double.NaN;
        }
        y.copyTo(yc, nb);
        int[] m = regarima.missing();
        if (m != null) {
            for (int i = 0; i < m.length; ++i) {
                yc[nb + m[i]] = Double.NaN;
            }
        }
        for (int i = nb + y.length(); i < yc.length; ++i) {
            yc[i] = Double.NaN;
        }
        ISsf ssf = Ssf.of(arima, SsfArima.defaultLoading());
        int nx = regarima.getVariablesCount();
        if (nx > 0) {
            FastMatrix x = FastMatrix.make(yc.length, nx);
            if (regarima.isMean()) {
                generateMeanEffect(regarima.arima().getNonStationaryAr(), x.column(0));
            }
            ssf = RegSsf.ssf(ssf, x);
        }
        DefaultSmoothingResults ss = DkToolkit.sqrtSmooth(ssf, new SsfData(yc), true, true);
        Results.Builder builder = Results.builder();
        ISsfLoading loading = ssf.loading();
        if (nb > 0) {
            double[] b = new double[nb], eb = new double[nb];
            for (int i = 0; i < nb; ++i) {
                b[i] = loading.ZX(i, ss.a(i));
                eb[i] = Math.sqrt(loading.ZVZ(i, ss.P(i)));
            }
            builder.backcasts(DoubleSeq.of(b)).backcastsErrors(DoubleSeq.of(eb));
        }
        if (nf > 0) {
            double[] f = new double[nf], ef = new double[nf];
            for (int i = 0, j = nb + y.length(); i < nf; ++i, ++j) {
                f[i] = loading.ZX(j, ss.a(j));
                ef[i] = Math.sqrt(loading.ZVZ(j, ss.P(j)));
            }
            builder.forecasts(DoubleSeq.of(f)).forecastsErrors(DoubleSeq.of(ef));
        }
        return builder.build();
    }

    public void generateMeanEffect(BackFilter ur, DataBlock m) {
        Polynomial p = ur.asPolynomial();
        int n = m.length();
        for (int i = p.degree(); i < n; ++i) {
            double c = 1;
            for (int j = 1; j <= p.degree(); ++j) {
                if (p.get(j) != 0) {
                    c -= p.get(j) * m.get(i - j);
                }
            }
            m.set(i, c);
        }
    }
}
