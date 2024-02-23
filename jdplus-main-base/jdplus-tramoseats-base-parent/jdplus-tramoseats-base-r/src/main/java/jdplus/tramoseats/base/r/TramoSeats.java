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
package jdplus.tramoseats.base.r;

import com.google.protobuf.InvalidProtocolBufferException;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.r.util.Dictionaries;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsDictionaries;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.base.protobuf.Spec;
import jdplus.tramoseats.base.protobuf.SpecProto;
import jdplus.tramoseats.base.protobuf.TramoSeatsProtosUtility;
import jdplus.tramoseats.base.protobuf.TramoSeatsResultsProto;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsOutput;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TramoSeats {

    public byte[] toBuffer(TramoSeatsResults rslts) {
        return TramoSeatsResultsProto.convert(rslts).toByteArray();
    }

    public TramoSeatsResults process(TsData series, String defSpec) {
        TramoSeatsSpec spec = TramoSeatsSpec.fromString(defSpec);
        TramoSeatsKernel kernel = TramoSeatsKernel.of(spec, null);
        return kernel.process(series.cleanExtremities(), null);
    }

    public TramoSeatsResults process(TsData series, TramoSeatsSpec spec, ModellingContext context) {
        TramoSeatsKernel kernel = TramoSeatsKernel.of(spec, context);
        return kernel.process(series.cleanExtremities(), null);
    }

    public TramoSeatsSpec refreshSpec(TramoSeatsSpec currentSpec, TramoSeatsSpec domainSpec, TsDomain domain, String policy) {
        return TramoSeatsFactory.getInstance().refreshSpec(currentSpec, domainSpec, EstimationPolicyType.valueOf(policy), domain);
    }

    public byte[] toBuffer(TramoSeatsSpec spec) {
        return SpecProto.convert(spec).toByteArray();
    }

    public TramoSeatsSpec specOf(byte[] buffer) {
        try {
            Spec spec = Spec.parseFrom(buffer);
            return SpecProto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public TramoSeatsOutput fullProcess(TsData series, TramoSeatsSpec spec, ModellingContext context) {
        TramoSeatsKernel tramoseats = TramoSeatsKernel.of(spec, context);
        TramoSeatsResults estimation = tramoseats.process(series.cleanExtremities(), null);

        return TramoSeatsOutput.builder()
                .estimationSpec(spec)
                .result(estimation)
                .resultSpec(estimation == null ? null : TramoSeatsFactory.getInstance().generateSpec(spec, estimation.getPreprocessing().getDescription()))
                .build();
    }

    public TramoSeatsOutput fullProcess(TsData series, String defSpec) {
        TramoSeatsSpec spec = TramoSeatsSpec.fromString(defSpec);
        return fullProcess(series, spec, null);
    }

    public byte[] toBuffer(TramoSeatsOutput output) {
        return TramoSeatsProtosUtility.convert(output).toByteArray();
    }

    public String[] dictionary() {
        return Dictionaries.entries(TramoSeatsDictionaries.TRAMOSEATSDICTIONARY);
    }

    public String[] fullDictionary() {
        return Dictionaries.all(TramoSeatsDictionaries.TRAMOSEATSDICTIONARY);
    }

}
