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
package jdplus.toolkit.base.protobuf.modelling;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Iterables;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.Range;
import jdplus.toolkit.base.core.modelling.StationaryTransformation;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtosUtility;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jdplus.toolkit.base.core.arima.IArimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ModellingProtosUtility {

    public LengthOfPeriodType convert(ModellingProtos.LengthOfPeriod lp) {
        return switch (lp) {
            case LP_LEAPYEAR -> LengthOfPeriodType.LeapYear;
            case LP_LENGTHOFPERIOD -> LengthOfPeriodType.LengthOfPeriod;
            default -> LengthOfPeriodType.None;
        };
    }

    public ModellingProtos.LengthOfPeriod convert(LengthOfPeriodType lp) {
        return switch (lp) {
            case LeapYear -> ModellingProtos.LengthOfPeriod.LP_LEAPYEAR;
            case LengthOfPeriod -> ModellingProtos.LengthOfPeriod.LP_LENGTHOFPERIOD;
            default -> ModellingProtos.LengthOfPeriod.LP_NONE;
        };
    }

    public ModellingProtos.TradingDays convert(TradingDaysType td) {
        return switch (td) {
            case TD7 -> ModellingProtos.TradingDays.TD7;
            case TD4 -> ModellingProtos.TradingDays.TD4;
            case TD3c -> ModellingProtos.TradingDays.TD3C;
            case TD3 -> ModellingProtos.TradingDays.TD3;
            case TD2c -> ModellingProtos.TradingDays.TD2C;
            case TD2 -> ModellingProtos.TradingDays.TD2;
            default -> ModellingProtos.TradingDays.TD_NONE;
        };
    }

    public TradingDaysType convert(ModellingProtos.TradingDays td) {
        return switch (td) {
            case TD7 -> TradingDaysType.TD7;
            case TD4 -> TradingDaysType.TD4;
            case TD3 -> TradingDaysType.TD3;
            case TD3C -> TradingDaysType.TD3c;
            case TD2C -> TradingDaysType.TD2c;
            case TD2 -> TradingDaysType.TD2;
            default -> TradingDaysType.NONE;
        };
    }

    public ModellingProtos.Transformation convert(TransformationType fn) {
        return switch (fn) {
            case Log -> ModellingProtos.Transformation.FN_LOG;
            case Auto -> ModellingProtos.Transformation.FN_AUTO;
            default -> ModellingProtos.Transformation.FN_LEVEL;
        };
    }

    public TransformationType convert(ModellingProtos.Transformation fn) {
        return switch (fn) {
            case FN_LOG -> TransformationType.Log;
            case FN_AUTO -> TransformationType.Auto;
            default -> TransformationType.None;
        };
    }

    public ModellingProtos.TsVariable convertTsContextVariable(Variable<TsContextVariable> v) {
        return ModellingProtos.TsVariable.newBuilder()
                .setName(v.getName())
                .setId(v.getCore().getId())
                .setLag(v.getCore().getLag())
                .setCoefficient(ToolkitProtosUtility.convert(v.getCoefficient(0)))
                .putAllMetadata(v.getAttributes())
                .build();
    }

    public Variable<TsContextVariable> convert(ModellingProtos.TsVariable v) {
        Parameter c = ToolkitProtosUtility.convert(v.getCoefficient());
        return Variable.<TsContextVariable>builder()
                .name(v.getName())
                .core(new TsContextVariable(v.getId(), v.getLag()))
                .attributes(v.getMetadataMap())
                 .coefficients(c == null ? null : new Parameter[]{c})
                .build();
    }

    public ModellingProtos.Ramp convertRamp(Variable<Ramp> v) {
        return ModellingProtos.Ramp.newBuilder()
                .setName(v.getName())
                .setStart(ToolkitProtosUtility.convert(v.getCore().getStart().toLocalDate()))
                .setEnd(ToolkitProtosUtility.convert(v.getCore().getEnd().toLocalDate()))
                .setCoefficient(ToolkitProtosUtility.convert(v.getCoefficient(0)))
                .putAllMetadata(v.getAttributes())
                .build();
    }

    public Variable<Ramp> convert(ModellingProtos.Ramp v) {
        LocalDate start = ToolkitProtosUtility.convert(v.getStart());
        LocalDate end = ToolkitProtosUtility.convert(v.getEnd());
        Parameter c = ToolkitProtosUtility.convert(v.getCoefficient());
        return Variable.<Ramp>builder()
                .name(v.getName())
                .core(new Ramp(start.atStartOfDay(), end.atStartOfDay()))
                .attributes(v.getMetadataMap())
                .coefficients(c == null ? null : new Parameter[]{c})
                .build();
    }

    public ModellingProtos.InterventionVariable convertInterventionVariable(Variable<InterventionVariable> var) {
        InterventionVariable v = var.getCore();
        ModellingProtos.InterventionVariable.Builder builder = ModellingProtos.InterventionVariable.newBuilder()
                .setName(var.getName())
                .setDelta(v.getDelta())
                .setSeasonalDelta(v.getDeltaSeasonal())
                .setCoefficient(ToolkitProtosUtility.convert(var.getCoefficient(0)))
                .putAllMetadata(var.getAttributes());

        List<Range<LocalDateTime>> seqs = v.getSequences();
        for (Range<LocalDateTime> seq :seqs) {
             builder.addSequences(ModellingProtos.InterventionVariable.Sequence.newBuilder()
                    .setStart(ToolkitProtosUtility.convert(seq.start().toLocalDate()))
                    .setEnd(ToolkitProtosUtility.convert(seq.end().toLocalDate()))
                    .build());
        }
        return builder.build();
    }

    public Variable<InterventionVariable> convert(ModellingProtos.InterventionVariable v) {
        InterventionVariable.Builder builder = InterventionVariable.builder()
                .delta(v.getDelta())
                .deltaSeasonal(v.getSeasonalDelta());
        int n = v.getSequencesCount();
        for (int i = 0; i < n; ++i) {
            ModellingProtos.InterventionVariable.Sequence seq = v.getSequences(i);
            LocalDate start = ToolkitProtosUtility.convert(seq.getStart());
            LocalDate end = ToolkitProtosUtility.convert(seq.getEnd());
            builder.sequence(Range.of(start.atStartOfDay(), end.atStartOfDay()));
        }
        Parameter c = ToolkitProtosUtility.convert(v.getCoefficient());
        return Variable.<InterventionVariable>builder()
                .name(v.getName())
                .core(builder.build())
                .coefficients(c == null ? null : new Parameter[]{c})
                .attributes(v.getMetadataMap())
                .build();
    }

    public ModellingProtos.StationaryTransformation convert(StationaryTransformation st) {
        ModellingProtos.StationaryTransformation.Builder builder = ModellingProtos.StationaryTransformation.newBuilder()
                .addAllStationarySeries(Iterables.of(st.getStationarySeries()))
                .setMeanCorrection(st.isMeanCorrection());
        for (StationaryTransformation.Differencing d : st.getDifferences()) {
            builder.addDifferences(ModellingProtos.StationaryTransformation.Differencing.newBuilder()
                    .setLag(d.getLag())
                    .setOrder(d.getOrder())
                    .build()
            );
        }
        return builder
                .build();
    }

    public StationaryTransformation convert(ModellingProtos.StationaryTransformation st) {
        double[] ds = new double[st.getStationarySeriesCount()];
        for (int i = 0; i < ds.length; ++i) {
            ds[i] = st.getStationarySeries(i);
        }
        StationaryTransformation.Builder builder = StationaryTransformation.builder()
                .stationarySeries(DoubleSeq.of(ds))
                .meanCorrection(st.getMeanCorrection());
        for (int i = 0; i < st.getDifferencesCount(); ++i) {
            ModellingProtos.StationaryTransformation.Differencing d = st.getDifferences(i);
            builder.difference(new StationaryTransformation.Differencing(d.getLag(), d.getOrder()));
        }
        return builder
                .build();
    }
    
    public ModellingProtos.ArimaModel convert(IArimaModel arima, String name){
        return ModellingProtos.ArimaModel.newBuilder()
                .setName(name)
                .addAllAr(Iterables.of(arima.getStationaryAr().asPolynomial().coefficients()))
                .addAllDelta(Iterables.of(arima.getNonStationaryAr().asPolynomial().coefficients()))
                .addAllMa(Iterables.of(arima.getMa().asPolynomial().coefficients()))
                .setInnovationVariance(arima.getInnovationVariance())
                .build();
                
    }

}
