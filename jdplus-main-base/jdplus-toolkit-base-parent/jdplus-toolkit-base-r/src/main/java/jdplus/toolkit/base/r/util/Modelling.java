/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.util;

import com.google.protobuf.InvalidProtocolBufferException;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.protobuf.toolkit.ModellingContextProto;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtos;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Modelling {
        public byte[] toBuffer(ModellingContext cntx) {
        return ModellingContextProto.convert(cntx).toByteArray();
    }

    public ModellingContext of(byte[] buffer) {
       try {
            ToolkitProtos.ModellingContext spec = ToolkitProtos.ModellingContext.parseFrom(buffer);
            return ModellingContextProto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

}
