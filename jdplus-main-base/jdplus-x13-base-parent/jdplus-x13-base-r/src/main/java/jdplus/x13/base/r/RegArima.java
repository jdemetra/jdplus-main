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
package jdplus.x13.base.r;

import com.google.protobuf.InvalidProtocolBufferException;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.toolkit.base.protobuf.regarima.RegArimaEstimationProto;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.x13.base.protobuf.RegArimaProto;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.StationaryTransformation;
import jdplus.toolkit.base.core.regsarima.regular.Forecast;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.x13.base.core.x13.regarima.DifferencingModule;
import jdplus.x13.base.core.x13.regarima.RegArimaFactory;
import jdplus.x13.base.core.x13.regarima.RegArimaKernel;
import jdplus.x13.base.core.x13.regarima.RegArimaOutput;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegArima {

    public byte[] toBuffer(RegSarimaModel core) {
        return RegArimaEstimationProto.convert(core).toByteArray();
    }

    public RegSarimaModel process(TsData series, String defSpec) {
        RegArimaSpec spec = RegArimaSpec.fromString(defSpec);
        RegArimaKernel regarima = RegArimaKernel.of(spec, null);
        return regarima.process(series.cleanExtremities(), null);
    }

    public RegSarimaModel process(TsData series, RegArimaSpec spec, ModellingContext context) {
        RegArimaKernel regarima = RegArimaKernel.of(spec, context);
        return regarima.process(series.cleanExtremities(), null);
    }

    public RegArimaSpec refreshSpec(RegArimaSpec currentSpec, RegArimaSpec domainSpec, TsDomain domain, String policy) {
        return RegArimaFactory.getInstance().refreshSpec(currentSpec, domainSpec, EstimationPolicyType.valueOf(policy), domain);
    }

    public Matrix forecast(TsData series, String defSpec, int nf) {
        RegArimaSpec spec = RegArimaSpec.fromString(defSpec);
        return forecast(series, spec, null, nf);
    }

    public Matrix forecast(TsData series, RegArimaSpec spec, ModellingContext context, int nf) {
        RegArimaKernel kernel = RegArimaKernel.of(spec, context);
        Forecast f = new Forecast(kernel, nf);
        if (!f.process(series.cleanExtremities())) {
            return null;
        }
        FastMatrix R = FastMatrix.make(nf, 4);
        R.column(0).copy(f.getForecasts());
        R.column(1).copy(f.getForecastsStdev());
        R.column(2).copy(f.getRawForecasts());
        R.column(3).copy(f.getRawForecastsStdev());
        return R;
    }

    public byte[] toBuffer(RegArimaSpec spec) {
        return RegArimaProto.convert(spec).toByteArray();
    }

    public RegArimaSpec specOf(byte[] buffer) {
        try {
            jdplus.x13.base.protobuf.RegArimaSpec spec = jdplus.x13.base.protobuf.RegArimaSpec.parseFrom(buffer);
            return RegArimaProto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    public RegArimaOutput fullProcess(TsData series, RegArimaSpec spec, ModellingContext context) {
        RegArimaKernel tramo = RegArimaKernel.of(spec, context);
        RegSarimaModel estimation = tramo.process(series.cleanExtremities(), null);

        return RegArimaOutput.builder()
                .estimationSpec(spec)
                .result(estimation)
                .resultSpec(estimation == null ? null : RegArimaFactory.getInstance().generateSpec(spec, estimation.getDescription()))
                .build();
    }

    public RegArimaOutput fullProcess(TsData series, String defSpec) {
        RegArimaSpec spec = RegArimaSpec.fromString(defSpec);
        return fullProcess(series, spec, null);
    }

    public byte[] toBuffer(RegArimaOutput output) {
        return RegArimaProto.convert(output).toByteArray();
    }

    public StationaryTransformation doStationary(double[] data, int period) {
        DifferencingModule diff = DifferencingModule.builder()
                .build();

        DoubleSeq s = DoubleSeq.of(data);
        diff.process(s, period);

        return StationaryTransformation.builder()
                .meanCorrection(diff.isMeanCorrection())
                .difference(new StationaryTransformation.Differencing(1, diff.getD()))
                .difference(new StationaryTransformation.Differencing(period, diff.getBd()))
                .build();
    }

}
