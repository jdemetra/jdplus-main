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
package jdplus.toolkit.base.protobuf.regarima;

import jdplus.toolkit.base.api.data.Iterables;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.api.timeseries.regression.IEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.ILengthOfPeriodVariable;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicOutlier;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.TransitoryChange;
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.protobuf.modelling.ModellingProtos;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtosUtility;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;
import jdplus.toolkit.base.protobuf.modelling.ModellingProtosUtility;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtos;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaEstimationProto {

    public RegArimaProtos.RegArimaModel.Description convert(GeneralLinearModel.Description<SarimaSpec> description) {

        RegArimaProtos.RegArimaModel.Description.Builder builder = RegArimaProtos.RegArimaModel.Description.newBuilder();

        TsDomain domain = description.getSeries().getDomain();
        Variable[] vars = description.getVariables();
        for (int i = 0; i < vars.length; ++i) {
            Variable vari = vars[i];
            int m = vari.dim();
            ITsVariable core = vari.getCore();
            ModellingProtos.VariableType type = type(core);
            ModellingProtos.RegressionVariable.Builder vbuilder = ModellingProtos.RegressionVariable.newBuilder()
                    .setName(vari.getName())
                    .setVarType(type)
                    .putAllMetadata(vars[i].getAttributes());
            for (int k = 0; k < m; ++k) {
                String pname = m == 1 ? vari.getName() : vari.getCore().description(k, domain);
                vbuilder.addCoefficients(ToolkitProtosUtility.convert(vari.getCoefficient(k), pname));
            }
            builder.addVariables(vbuilder.build());
        }

        return builder.setSeries(ToolkitProtosUtility.convert(description.getSeries()))
                .setPreadjustment(ModellingProtosUtility.convert(description.getLengthOfPeriodTransformation()))
                .setLog(description.isLogTransformation())
                .setArima(RegArimaProtosUtility.convert(description.getStochasticComponent()))
                .build();
    }

    public RegArimaProtos.RegArimaModel.Estimation convert(GeneralLinearModel.Estimation estimation, DoubleSeq res) {
        RegArimaProtos.RegArimaModel.Estimation.Builder builder = RegArimaProtos.RegArimaModel.Estimation.newBuilder();

        Matrix cov = estimation.getCoefficientsCovariance();
        LikelihoodStatistics statistics = estimation.getStatistics();

        builder.addAllY(Iterables.of(estimation.getY()))
                .setX(ToolkitProtosUtility.convert(estimation.getX()))
                .setParameters(ToolkitProtosUtility.convert(estimation.getParameters()))
                .setLikelihood(ToolkitProtosUtility.convert(statistics))
                .addAllB(Iterables.of(estimation.getCoefficients()))
                .setBcovariance(ToolkitProtosUtility.convert(cov))
                .addAllResiduals(Iterables.of(res));

        // missing
        MissingValueEstimation[] missing = estimation.getMissing();
        if (missing.length > 0) {
            for (int i = 0; i < missing.length; ++i) {
                builder.addMissings(convert(missing[i]));
            }
        }
        return builder.build();
    }

    public RegArimaProtos.RegArimaModel.Estimation convert(RegArimaEstimation<SarimaModel> estimation) {
        RegArimaProtos.RegArimaModel.Estimation.Builder builder = RegArimaProtos.RegArimaModel.Estimation.newBuilder();
        Matrix cov = estimation.getConcentratedLikelihood().covariance(estimation.parametersCount(), true);
        LikelihoodStatistics statistics = estimation.statistics();
        RegArimaModel<SarimaModel> model = estimation.getModel();
        LogLikelihoodFunction.Point<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> max = estimation.getMax();
        ParametersEstimation pestim;
        if (max == null) {
            pestim = new ParametersEstimation(Doubles.EMPTY, FastMatrix.EMPTY, Doubles.EMPTY, null);
        } else {
            pestim = new ParametersEstimation(max.getParameters(), max.asymptoticCovariance(), max.getScore(), "sarima (true signs)");
        }

        // complete for missings
        builder.addAllY(Iterables.of(model.getY()))
                .setX(ToolkitProtosUtility.convert(model.variables()))
                .setParameters(ToolkitProtosUtility.convert(pestim))
                .setLikelihood(ToolkitProtosUtility.convert(statistics))
                .addAllB(Iterables.of(estimation.getConcentratedLikelihood().coefficients()))
                .setBcovariance(ToolkitProtosUtility.convert(cov))
                .addAllResiduals(Iterables.of(estimation.getConcentratedLikelihood().e()));

        // TODO: missing
        return builder.build();
    }

    public RegArimaProtos.RegArimaModel convert(RegSarimaModel model) {
        if (model == null) {
            return RegArimaProtos.RegArimaModel.newBuilder().build();
        }
        DoubleSeq res = model.fullResiduals().getValues();
        return RegArimaProtos.RegArimaModel.newBuilder()
                .setDescription(convert(model.getDescription()))
                .setEstimation(convert(model.getEstimation(), res))
                .setDiagnostics(diagnosticsOf(model))
                .build();

    }

    public ModellingProtos.MissingEstimation convert(MissingValueEstimation missing) {
        return ModellingProtos.MissingEstimation.newBuilder()
                .setPosition(missing.getPosition())
                .setValue(missing.getValue())
                .setStde(missing.getStandardError())
                .build();
    }

    public ModellingProtos.Diagnostics diagnosticsOf(GeneralLinearModel<SarimaSpec> model) {

        ModellingProtos.Diagnostics.Builder builder = ModellingProtos.Diagnostics.newBuilder();
        model.getResiduals().getTests().forEach((k, v)
                -> {
            if (v instanceof StatisticalTest) {
                StatisticalTest st = (StatisticalTest) v;
                if (st.isValid()) {
                    ToolkitProtos.StatisticalTest test = ToolkitProtosUtility.convert(st);
                    builder.putResidualsTests(k, test);
                }
            }
        });
        return builder.build();
    }

    public ModellingProtos.VariableType type(ITsVariable var) {
        if (var instanceof TrendConstant) {
            return ModellingProtos.VariableType.VAR_MEAN;
        }
        if (var instanceof ITradingDaysVariable) {
            return ModellingProtos.VariableType.VAR_TD;
        }
        if (var instanceof ILengthOfPeriodVariable) {
            return ModellingProtos.VariableType.VAR_LP;
        }
        if (var instanceof IEasterVariable) {
            return ModellingProtos.VariableType.VAR_EASTER;
        }
        if (var instanceof IOutlier) {
            switch (((IOutlier) var).getCode()) {
                case AdditiveOutlier.CODE:
                    return ModellingProtos.VariableType.VAR_AO;
                case LevelShift.CODE:
                    return ModellingProtos.VariableType.VAR_LS;
                case TransitoryChange.CODE:
                    return ModellingProtos.VariableType.VAR_TC;
                case PeriodicOutlier.CODE:
                case PeriodicOutlier.PO:
                    return ModellingProtos.VariableType.VAR_SO;
                default:
                    return ModellingProtos.VariableType.VAR_OUTLIER;
            }
        }
        if (var instanceof InterventionVariable) {
            return ModellingProtos.VariableType.VAR_IV;
        }
        if (var instanceof Ramp) {
            return ModellingProtos.VariableType.VAR_RAMP;
        }
        return ModellingProtos.VariableType.VAR_UNSPECIFIED;
    }

}
