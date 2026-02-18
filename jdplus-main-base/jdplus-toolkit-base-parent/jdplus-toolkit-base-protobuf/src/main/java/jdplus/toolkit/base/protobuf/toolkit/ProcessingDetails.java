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
package jdplus.toolkit.base.protobuf.toolkit;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Iterables;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ProcessingDetails {

    ToolkitProtos.ProcessingDetail of(double dvalue) {
        return ToolkitProtos.ProcessingDetail.newBuilder()
                .setDvalue(dvalue)
                .build();
    }

    ToolkitProtos.ProcessingDetail of(int ivalue) {
        return ToolkitProtos.ProcessingDetail.newBuilder()
                .setDvalue(ivalue)
                .build();
    }

    ToolkitProtos.ProcessingDetail of(StatisticalTest test) {
        return ToolkitProtos.ProcessingDetail.newBuilder()
                .setTest(ToolkitProtosUtility.convert(test))
                .build();
    }

    ToolkitProtos.ProcessingDetail of(TsData ts) {
        return ToolkitProtos.ProcessingDetail.newBuilder()
                .setTs(ToolkitProtosUtility.convert(ts))
                .build();
    }

    ToolkitProtos.ProcessingDetail of(Matrix matrix) {
        return ToolkitProtos.ProcessingDetail.newBuilder()
                .setMatrix(ToolkitProtosUtility.convert(matrix))
                .build();
    }

    ToolkitProtos.ProcessingDetail of(DoubleSeq doubles) {
        return ToolkitProtos.ProcessingDetail.newBuilder()
                .setArray(ToolkitProtos.Doubles.newBuilder()
                        .addAllValues(Iterables.of(doubles))
                        .build())
                .build();
    }

    ToolkitProtos.ProcessingDetail of(Object data) {
        if (data == null) {
            return null;
        } else if (data instanceof TsData tsData) {
            return of(tsData);
        } else if (data instanceof Matrix matrix) {
            return of(matrix);
        } else if (data instanceof DoubleSeq seq) {
            return of(seq);
        } else if (data instanceof StatisticalTest test) {
            return of(test);
        } else if (data instanceof Double double1) {
            return of(double1);
        } else if (data instanceof Integer integer) {
            return of(integer);
        }
        return null;
    }

}
