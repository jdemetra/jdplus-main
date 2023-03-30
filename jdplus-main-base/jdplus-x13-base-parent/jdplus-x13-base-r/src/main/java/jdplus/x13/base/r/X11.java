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
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.x13.base.core.x11.X11Results;
import jdplus.x13.base.api.x11.X11Spec;
import jdplus.x13.base.protobuf.X11Proto;
import jdplus.x13.base.protobuf.X11ResultsProto;
import jdplus.x13.base.core.x11.X11Kernel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X11 {

    public X11Results process(TsData series, X11Spec spec) {
        X11Kernel kernel = new X11Kernel();
        return kernel.process(series.cleanExtremities(), spec);
    }

    public byte[] toBuffer(X11Results core) {
        return X11ResultsProto.convert(core).toByteArray();
    }

    public byte[] toBuffer(X11Spec spec) {
        return X11Proto.convert(spec).toByteArray();
    }

    public X11Spec of(byte[] buffer) {
        try {
            jdplus.x13.base.protobuf.X11Spec spec = jdplus.x13.base.protobuf.X11Spec.parseFrom(buffer);
            return X11Proto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }
}
