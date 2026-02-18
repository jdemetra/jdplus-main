/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.base.protobuf;

import jdplus.toolkit.base.api.data.Iterables;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.sa.base.api.StationaryVarianceDecomposition;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import jdplus.sa.base.api.diagnostics.CombinedSeasonalityTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.protobuf.modelling.ModellingProtos;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtosUtility;
import jdplus.sa.base.core.diagnostics.GenericSaTests;
import jdplus.sa.base.core.tests.CombinedSeasonality;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaProtosUtility {

    public SaBenchmarkingSpec.Target convert(SaProtos.BenchmarkingTarget t) {
        return switch (t) {
            case BENCH_TARGET_ORIGINAL -> SaBenchmarkingSpec.Target.Original;
            default -> SaBenchmarkingSpec.Target.CalendarAdjusted;
        };
    }

    public SaProtos.BenchmarkingTarget convert(SaBenchmarkingSpec.Target t) {
        return switch (t) {
            case Original -> SaProtos.BenchmarkingTarget.BENCH_TARGET_ORIGINAL;
            case CalendarAdjusted -> SaProtos.BenchmarkingTarget.BENCH_TARGET_CALENDARADJUSTED;
            default -> SaProtos.BenchmarkingTarget.UNRECOGNIZED;
        };
    }

    public SaProtos.BenchmarkingBias convert(SaBenchmarkingSpec.BiasCorrection t) {
        return switch (t) {
            case None -> SaProtos.BenchmarkingBias.BENCH_BIAS_NONE;
            case Additive -> SaProtos.BenchmarkingBias.BENCH_BIAS_ADDITIVE;
            case Multiplicative -> SaProtos.BenchmarkingBias.BENCH_BIAS_MULTIPLICATIVE;
            default -> SaProtos.BenchmarkingBias.UNRECOGNIZED;
        };
    }

    public SaBenchmarkingSpec.BiasCorrection convert(SaProtos.BenchmarkingBias t) {
        return switch (t) {
            case BENCH_BIAS_ADDITIVE -> SaBenchmarkingSpec.BiasCorrection.Additive;
            case BENCH_BIAS_MULTIPLICATIVE -> SaBenchmarkingSpec.BiasCorrection.Multiplicative;
            default -> SaBenchmarkingSpec.BiasCorrection.None;
        };
    }

    public SaBenchmarkingSpec convert(SaProtos.BenchmarkingSpec spec) {
        return SaBenchmarkingSpec.builder()
                .enabled(spec.getEnabled())
                .target(convert(spec.getTarget()))
                .lambda(spec.getLambda())
                .rho(spec.getRho())
                .biasCorrection(convert(spec.getBias()))
                .forecast(spec.getForecast())
                .build();
    }

    public void fill(SaBenchmarkingSpec spec, SaProtos.BenchmarkingSpec.Builder builder) {
        builder.setEnabled(spec.isEnabled())
                .setTarget(convert(spec.getTarget()))
                .setLambda(spec.getLambda())
                .setRho(spec.getRho())
                .setBias(convert(spec.getBiasCorrection()))
                .setForecast(spec.isForecast());
    }

    public SaProtos.BenchmarkingSpec convert(SaBenchmarkingSpec spec) {
        SaProtos.BenchmarkingSpec.Builder builder = SaProtos.BenchmarkingSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public SaProtos.SaDecomposition convert(SeriesDecomposition decomp) {
        SaProtos.SaDecomposition.Builder builder = SaProtos.SaDecomposition.newBuilder();
        // Series

        return builder
                .setSeries(convert(decomp, ComponentType.Series))
                .setSeasonallyAdjusted(convert(decomp, ComponentType.SeasonallyAdjusted))
                .setTrend(convert(decomp, ComponentType.Trend))
                .setSeasonal(convert(decomp, ComponentType.Seasonal))
                .setIrregular(convert(decomp, ComponentType.Irregular))
                .build();
    }

    public ModellingProtos.TsComponent convert(SeriesDecomposition decomp, ComponentType type) {
        TsData s = decomp.getSeries(type, ComponentInformation.Value);
        TsData fs = decomp.getSeries(type, ComponentInformation.Forecast);
        TsData bs = decomp.getSeries(type, ComponentInformation.Backcast);
        TsData es = decomp.getSeries(type, ComponentInformation.Stdev);
        TsData efs = decomp.getSeries(type, ComponentInformation.StdevForecast);
        TsData ebs = decomp.getSeries(type, ComponentInformation.StdevBackcast);
        if (es == null && s != null){
            es=TsData.of(s.getStart(), DoubleSeq.onMapping(s.length(), i->0));
        }
        TsData S = TsData.concatenate(bs, s, fs);
        TsData ES = TsData.concatenate(ebs, es, efs);

        ModellingProtos.TsComponent.Builder builder = ModellingProtos.TsComponent.newBuilder()
                .setData(ToolkitProtosUtility.convert(S))
                .setNbcasts(bs == null ? 0 : bs.length())
                .setNfcasts(fs == null ? 0 : fs.length());
        if (ES != null) {
            builder.addAllStde(Iterables.of(ES.getValues()));
        }

        return builder.build();
    }

    public SaProtos.ComponentType convert(ComponentType type) {
        return switch (type) {
            case Series -> SaProtos.ComponentType.SERIES;
            case SeasonallyAdjusted -> SaProtos.ComponentType.SEASONALLYADJUSTED;
            case Trend -> SaProtos.ComponentType.TREND;
            case Seasonal -> SaProtos.ComponentType.SEASONAL;
            case Irregular -> SaProtos.ComponentType.IRREGULAR;
            default -> SaProtos.ComponentType.UNDEFINED;
        };
    }

    public SaProtos.VarianceDecomposition convert(StationaryVarianceDecomposition var) {
        return SaProtos.VarianceDecomposition.newBuilder()
                .setCycle(var.getC())
                .setSeasonal(var.getS())
                .setIrregular(var.getI())
                .setCalendar(var.getCalendar())
                .setOthers(var.getP())
                .setTotal(var.total())
                .build();
    }

    public SaProtos.Diagnostics of(GenericSaTests tests, StationaryVarianceDecomposition var) {
        return SaProtos.Diagnostics.newBuilder()
                .setSeasonalFtestOnIrregular(ToolkitProtosUtility.convert(tests.residualSeasonalityTestsOnIrregular().fTest()))
                .setSeasonalFtestOnSa(ToolkitProtosUtility.convert(tests.residualSeasonalityTestsOnSa().fTest()))
                .setSeasonalQtestOnIrregular(ToolkitProtosUtility.convert(tests.residualSeasonalityTestsOnIrregular().qsTest()))
                .setSeasonalQtestOnSa(ToolkitProtosUtility.convert(tests.residualSeasonalityTestsOnSa().qsTest()))
                .setTdFtestOnIrregular(ToolkitProtosUtility.convert(tests.residualTradingDaysTests().irrTest(false)))
                .setTdFtestOnSa(ToolkitProtosUtility.convert(tests.residualTradingDaysTests().saTest(false)))
                .setVarianceDecomposition(SaProtosUtility.convert(var))
                .build();

    }

    public SaProtos.EstimationPolicy convert(EstimationPolicyType policy) {
        return switch (policy) {
            case Complete -> SaProtos.EstimationPolicy.POLICY_COMPLETE;
            case Outliers_StochasticComponent -> SaProtos.EstimationPolicy.POLICY_ARIMA;
            case Outliers -> SaProtos.EstimationPolicy.POLICY_OUTLIERS;
            case LastOutliers -> SaProtos.EstimationPolicy.POLICY_LASTOUTLIERS;
            case FreeParameters -> SaProtos.EstimationPolicy.POLICY_FREE_PARAMETERS;
            case FixedAutoRegressiveParameters -> SaProtos.EstimationPolicy.POLICY_FIXED_AUTOREGRESSIVEPARAMETERS;
            case FixedParameters -> SaProtos.EstimationPolicy.POLICY_FIXED_PARAMETERS;
            case Fixed -> SaProtos.EstimationPolicy.POLICY_FIXED;
            case Current -> SaProtos.EstimationPolicy.POLICY_CURRENT;
            default -> SaProtos.EstimationPolicy.UNRECOGNIZED;
        };
    }

    public EstimationPolicyType convert(SaProtos.EstimationPolicy policy) {
        return switch (policy) {
            case POLICY_COMPLETE -> EstimationPolicyType.Complete;
            case POLICY_ARIMA -> EstimationPolicyType.Outliers_StochasticComponent;
            case POLICY_OUTLIERS -> EstimationPolicyType.Outliers;
            case POLICY_LASTOUTLIERS -> EstimationPolicyType.LastOutliers;
            case POLICY_FREE_PARAMETERS -> EstimationPolicyType.FreeParameters;
            case POLICY_FIXED_AUTOREGRESSIVEPARAMETERS -> EstimationPolicyType.FixedAutoRegressiveParameters;
            case POLICY_FIXED_PARAMETERS -> EstimationPolicyType.FixedParameters;
            case POLICY_FIXED -> EstimationPolicyType.Fixed;
            case POLICY_CURRENT -> EstimationPolicyType.Current;
            default -> EstimationPolicyType.None;
        };
    }
    
    public SaProtos.IdentifiableSeasonality convert(CombinedSeasonalityTest.IdentifiableSeasonality seas){
        return switch (seas){
            case None -> SaProtos.IdentifiableSeasonality.SA_NONE;
            case ProbablyNone -> SaProtos.IdentifiableSeasonality.SA_PROBABLY_NONE;
            case Present -> SaProtos.IdentifiableSeasonality.SA_PRESENT;
            default -> SaProtos.IdentifiableSeasonality.SA_UNKNOWN;
        };
    }
    
    public SaProtos.CombinedSeasonalityTest convert(CombinedSeasonality cs){
        return SaProtos.CombinedSeasonalityTest.newBuilder()
                .setKruskalWallis(ToolkitProtosUtility.convert(cs.getNonParametricTestForStableSeasonality().build()))
                .setStableSeasonality(ToolkitProtosUtility.convert(cs.getStableSeasonalityTest()))
                .setEvolutiveSeasonality(ToolkitProtosUtility.convert(cs.getEvolutiveSeasonalityTest()))
                .setSeasonality(convert(cs.getSummary()))
                .build();
    }

}
