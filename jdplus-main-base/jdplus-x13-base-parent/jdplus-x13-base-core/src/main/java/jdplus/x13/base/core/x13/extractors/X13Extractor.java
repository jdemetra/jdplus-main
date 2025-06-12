/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x13.extractors;

import java.util.ArrayList;
import java.util.List;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.x13.base.api.x13.X13Dictionaries;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.processing.ProcQuality;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.api.util.IntList;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.x13.base.core.x11.X11Results;
import jdplus.x13.base.core.x13.X13Diagnostics;
import jdplus.x13.base.core.x13.X13Factory;
import jdplus.x13.base.core.x13.X13Results;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X13Extractor extends InformationMapping<X13Results> {

    private String decompositionItem(String key) {
        return Dictionary.concatenate(SaDictionaries.DECOMPOSITION, key);
    }

    private String preadjustItem(String key) {
        return Dictionary.concatenate(X13Dictionaries.PREADJUST, key);
    }

    private String finalItem(String key) {
        return Dictionary.concatenate(X13Dictionaries.FINAL, key);
    }

    private String advancedItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.ADVANCED, key);
    }

    private String qualityItem(String key) {
        return Dictionary.concatenate(SaDictionaries.QUALITY, key);
    }

    public X13Extractor() {
//         MAPPING.set(FINAL + ModellingDictionary.Y, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));
//
//        MAPPING.set(FINAL + SaDictionaries.T, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));
//
        set(SaDictionaries.MODE, DecompositionMode.class, source -> source.getDecomposition() == null ? null : source.getDecomposition().getMode());

        set(SaDictionaries.SEASONAL, Integer.class, source -> {
            if (source.getDecomposition() == null) {
                return null;
            }
            TsData s = source.getDecomposition().getD10();
            if (s == null) {
                return 0;
            } else {
                double x0 = s.getValue(0);
                return s.getValues().allMatch(x -> x == x0) ? 0 : 1;
            }
        }
        );

        set(preadjustItem(X13Dictionaries.A1), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA1());
        set(preadjustItem(X13Dictionaries.A1A), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA1a());
        set(preadjustItem(X13Dictionaries.A1B), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA1b());
        set(preadjustItem(X13Dictionaries.A6), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA6());
        set(preadjustItem(X13Dictionaries.A7), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA7());
        set(preadjustItem(X13Dictionaries.A8), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA8());
        set(preadjustItem(X13Dictionaries.A8I), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA8i());
        set(preadjustItem(X13Dictionaries.A8S), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA8s());
        set(preadjustItem(X13Dictionaries.A8T), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA8t());
        set(preadjustItem(X13Dictionaries.A9), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA9());
        set(preadjustItem(X13Dictionaries.A9SA), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA9sa());
        set(preadjustItem(X13Dictionaries.A9U), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA9u());
        set(preadjustItem(X13Dictionaries.A9SER), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA9ser());
        set(preadjustItem(X13Dictionaries.A9CAL), TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA9cal());
        set(SaDictionaries.S, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD16());
        set(SaDictionaries.S + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD16a());
        set(SaDictionaries.S + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD16b());
        set(SaDictionaries.SA, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD11final());
        set(SaDictionaries.SA + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD11a());
        set(SaDictionaries.SA + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD11b());
        set(SaDictionaries.T, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD12final());
        set(SaDictionaries.T + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD12a());
        set(SaDictionaries.T + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD12b());
        set(SaDictionaries.I, TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD13final());
        set(SaDictionaries.I + SeriesInfo.F_SUFFIX, TsData.class, source
                -> {
            if (source.getFinals() == null) {
                return null;
            }
            TsData d12a = source.getFinals().getD12a();
            if (d12a == null || d12a.size() == 0) {
                return null;
            }
            DecompositionMode mode = source.getDecomposition().getMode();
            double m = mode.isMultiplicative() ? 1 : 0;
            return TsData.of(d12a.getStart(), DoubleSeq.onMapping(d12a.size(), i -> m));
        }
        );
        set(SaDictionaries.I + SeriesInfo.B_SUFFIX, TsData.class, source
                -> {
            if (source.getFinals() == null) {
                return null;
            }
            TsData d12b = source.getFinals().getD12b();
            if (d12b == null || d12b.size() == 0) {
                return null;
            }
            DecompositionMode mode = source.getDecomposition().getMode();
            double m = mode.isMultiplicative() ? 1 : 0;
            return TsData.of(d12b.getStart(), DoubleSeq.onMapping(d12b.size(), i -> m));
        }
        );
        set(decompositionItem(SaDictionaries.Y_CMP), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getB1(), source.getDecomposition().getActualDomain()));
        set(decompositionItem(SaDictionaries.Y_CMP_F), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getB1(), source.getDecomposition().getForecastDomain()));
        set(decompositionItem(SaDictionaries.Y_CMP_B), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getB1(), source.getDecomposition().getBackcastDomain()));
        set(decompositionItem(SaDictionaries.S_CMP), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD10(), source.getDecomposition().getActualDomain()));
        set(decompositionItem(SaDictionaries.S_CMP + SeriesInfo.F_SUFFIX), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD10(), source.getDecomposition().getForecastDomain()));
        set(decompositionItem(SaDictionaries.S_CMP + SeriesInfo.B_SUFFIX), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD10(), source.getDecomposition().getBackcastDomain()));
        set(decompositionItem(SaDictionaries.SA_CMP), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD11(), source.getDecomposition().getActualDomain()));
        set(decompositionItem(SaDictionaries.SA_CMP + SeriesInfo.F_SUFFIX), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD11(), source.getDecomposition().getForecastDomain()));
        set(decompositionItem(SaDictionaries.SA_CMP + SeriesInfo.B_SUFFIX), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD11(), source.getDecomposition().getBackcastDomain()));
        set(decompositionItem(SaDictionaries.T_CMP), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD12(), source.getDecomposition().getActualDomain()));
        set(decompositionItem(SaDictionaries.T_CMP + SeriesInfo.F_SUFFIX), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD12(), source.getDecomposition().getForecastDomain()));
        set(decompositionItem(SaDictionaries.T_CMP + SeriesInfo.B_SUFFIX), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD12(), source.getDecomposition().getBackcastDomain()));
        set(decompositionItem(SaDictionaries.I_CMP), TsData.class, source
                -> source.getDecomposition() == null ? null : source.getDecomposition().getD13());
        set(decompositionItem(SaDictionaries.SI_CMP), TsData.class, source
                -> source.getDecomposition() == null ? null : TsData.fitToDomain(source.getDecomposition().getD8(), source.getDecomposition().getActualDomain()));

        set(SaDictionaries.Y, TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA1());
        set(SaDictionaries.Y + SaDictionaries.BACKCAST, TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA1b());
        set(SaDictionaries.Y + SaDictionaries.FORECAST, TsData.class, source
                -> source.getPreadjustment() == null ? null : source.getPreadjustment().getA1a());

//        set(RegressionDictionaries.CAL, TsData.class, source
//                -> source.getPreprocessing() == null ? null : source.getPreprocessing().getCalendarEffect(source.getDecomposition().getActualDomain()));
//        set(RegressionDictionaries.CAL_B, TsData.class, source
//                -> source.getPreprocessing() == null ? null : source.getPreprocessing().getCalendarEffect(source.getDecomposition().getBackcastDomain()));
//        set(RegressionDictionaries.CAL_F, TsData.class, source
//                -> source.getPreprocessing() == null ? null : source.getPreprocessing().getCalendarEffect(source.getDecomposition().getForecastDomain()));

        set(finalItem(X13Dictionaries.D11), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD11final());
        set(finalItem(X13Dictionaries.D12), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD12final());
        set(finalItem(X13Dictionaries.D13), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD13final());
        set(finalItem(X13Dictionaries.D16), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD16());
        set(finalItem(X13Dictionaries.D18), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD18());
        set(finalItem(X13Dictionaries.D11A), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD11a());
        set(finalItem(X13Dictionaries.D12A), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD12a());
        set(finalItem(X13Dictionaries.D16A), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD16a());
        set(finalItem(X13Dictionaries.D18A), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD18a());
        set(finalItem(X13Dictionaries.D11B), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD11b());
        set(finalItem(X13Dictionaries.D12B), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD12b());
        set(finalItem(X13Dictionaries.D16B), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD16b());
        set(finalItem(X13Dictionaries.D18B), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getD18b());

        set(finalItem(X13Dictionaries.E1), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getE1());
        set(finalItem(X13Dictionaries.E2), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getE2());
        set(finalItem(X13Dictionaries.E3), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getE3());
        set(finalItem(X13Dictionaries.E11), TsData.class, source
                -> source.getFinals() == null ? null : source.getFinals().getE11());

        set(advancedItem(RegressionDictionaries.REGTYPE), int[].class,
                source -> {
                    if (source.getPreprocessing() == null) {
                        return null;
                    }
                    GeneralLinearModel.Description<SarimaSpec> desc = source.getPreprocessing().getDescription();
                    Variable[] vars = desc.getVariables();
                    IntList list = new IntList();
                    for (int i = 0; i < vars.length; ++i) {
                        int n = vars[i].freeCoefficientsCount();
                        if (n > 0) {
                            ComponentType regressionEffect = SaVariable.regressionEffect(vars[i]);
                            for (int j = 0; j < n; ++j) {
                                list.add(regressionEffect.toInt());
                            }
                        }
                    }
                    return list.toArray();
                });
        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());

        delegate(SaDictionaries.DECOMPOSITION, X11Results.class, source -> source.getDecomposition());

        delegate(null, X13Diagnostics.class, source -> source.getDiagnostics());

        set(qualityItem(SaDictionaries.QUALITY_SUMMARY), String.class, source -> {
            List<ProcDiagnostic> tests = new ArrayList<>();
            X13Factory.getInstance().fillDiagnostics(tests, null, source);
            ProcQuality quality = ProcDiagnostic.summary(tests);
            return quality.name();
        });

        delegate(SaDictionaries.BENCHMARKING, SaBenchmarkingResults.class, source -> source.getBenchmarking());

    }

    @Override
    public Class getSourceClass() {
        return X13Results.class;
    }
}
