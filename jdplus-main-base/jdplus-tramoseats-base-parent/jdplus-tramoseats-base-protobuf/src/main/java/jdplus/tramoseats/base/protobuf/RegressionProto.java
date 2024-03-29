/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.protobuf;

import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.protobuf.modelling.ModellingProtos;
import jdplus.toolkit.base.protobuf.modelling.ModellingProtosUtility;
import jdplus.tramoseats.base.api.tramo.RegressionSpec;
import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicOutlier;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.TransitoryChange;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtosUtility;
import jdplus.tramoseats.base.api.tramo.CalendarSpec;
import jdplus.tramoseats.base.api.tramo.MeanSpec;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegressionProto {

    public RegressionSpec convert(TramoSpec.RegressionSpec spec, double tc) {
        CalendarSpec.Builder cbuilder = CalendarSpec.builder();
        if (spec.hasEaster()) {
            cbuilder.easter(EasterProto.convert(spec.getEaster()));
        }
        if (spec.hasTd()) {
            cbuilder.tradingDays(TradingDaysProto.convert(spec.getTd()));
        }

        MeanSpec mean = MeanSpec.none();
        if (spec.hasMean()) {
            Parameter p = ToolkitProtosUtility.convert(spec.getMean());
            if (p != null) {
                boolean check = spec.getCheckMean();
                mean = MeanSpec.builder()
                        .trendConstant(true)
                        .test(check)
                        .coefficient(ToolkitProtosUtility.convert(spec.getMean()))
                        .build();
            }
        }

        RegressionSpec.Builder builder = RegressionSpec.builder()
                .mean(mean)
                .calendar(cbuilder.build());
        int n = spec.getOutliersCount();
        for (int i = 0; i < n; ++i) {
            ModellingProtos.Outlier outlier = spec.getOutliers(i);
            builder.outlier(convert(outlier, tc));
        }
        n = spec.getUsersCount();
        for (int i = 0; i < n; ++i) {
            ModellingProtos.TsVariable var = spec.getUsers(i);
            builder.userDefinedVariable(ModellingProtosUtility.convert(var));
        }
        n = spec.getInterventionsCount();
        for (int i = 0; i < n; ++i) {
            ModellingProtos.InterventionVariable var = spec.getInterventions(i);
            builder.interventionVariable(ModellingProtosUtility.convert(var));
        }
        n = spec.getRampsCount();
        for (int i = 0; i < n; ++i) {
            ModellingProtos.Ramp var = spec.getRamps(i);
            builder.ramp(ModellingProtosUtility.convert(var));
        }

        return builder.build();
    }

    public TramoSpec.RegressionSpec convert(RegressionSpec spec) {
        TramoSpec.RegressionSpec.Builder builder = TramoSpec.RegressionSpec.newBuilder()
                .setEaster(EasterProto.convert(spec.getCalendar().getEaster()))
                .setTd(TradingDaysProto.convert(spec.getCalendar().getTradingDays()));
        MeanSpec mean = spec.getMean();
        if (mean.isUsed()) {
            builder.setMean(ToolkitProtosUtility.convert(mean.getCoefficient()))
                    .setCheckMean(mean.isTest());
        } else {
            // unused -> Parameter = unused
            builder.clearMean().setCheckMean(false);
        }

        List<Variable<IOutlier>> outliers = spec.getOutliers();
        outliers.forEach(outlier -> {
            builder.addOutliers(convert(outlier));
        });
        List<Variable<TsContextVariable>> users = spec.getUserDefinedVariables();
        users.forEach(user -> {
            builder.addUsers(ModellingProtosUtility.convertTsContextVariable(user));
        });
        List<Variable<InterventionVariable>> ivs = spec.getInterventionVariables();
        ivs.forEach(iv -> {
            builder.addInterventions(ModellingProtosUtility.convertInterventionVariable(iv));
        });
        List<Variable<Ramp>> ramps = spec.getRamps();
        ramps.forEach(ramp -> {
            builder.addRamps(ModellingProtosUtility.convertRamp(ramp));
        });

        return builder.build();
    }

    public Variable<IOutlier> convert(ModellingProtos.Outlier outlier, double tc) {
        LocalDate ldt = ToolkitProtosUtility.convert(outlier.getPosition());
        IOutlier o;
        switch (outlier.getCode()) {
            case "ao":
            case "AO":
                o = new AdditiveOutlier(ldt.atStartOfDay());
                break;
            case "ls":
            case "LS":
                o = new LevelShift(ldt.atStartOfDay(), true);
                break;
            case "tc":
            case "TC":
                o = new TransitoryChange(ldt.atStartOfDay(), tc);
                break;
            case "so":
            case "SO":
                o = new PeriodicOutlier(ldt.atStartOfDay(), 0, true);
                break;

            default:
                return null;
        }
        Parameter c = ToolkitProtosUtility.convert(outlier.getCoefficient());
        return Variable.<IOutlier>builder()
                .core(o)
                .name(outlier.getName())
                .coefficients(c == null ? null : new Parameter[]{c})
                .attributes(outlier.getMetadataMap())
                .build();
    }

    public ModellingProtos.Outlier convert(Variable<IOutlier> v) {
        IOutlier outlier = v.getCore();
        return ModellingProtos.Outlier.newBuilder()
                .setName(v.getName())
                .setCode(outlier.getCode())
                .setPosition(ToolkitProtosUtility.convert(outlier.getPosition().toLocalDate()))
                .setCoefficient(ToolkitProtosUtility.convert(v.getCoefficient(0)))
                .build();
    }
}
