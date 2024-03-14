/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.util;

import com.google.protobuf.InvalidProtocolBufferException;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarManager;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import jdplus.toolkit.base.protobuf.toolkit.CalendarProtosUtility;
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

    public byte[] toBuffer(CalendarDefinition cntx) {
        return CalendarProtosUtility.convert(cntx).toByteArray();
    }

    public CalendarDefinition calendarOf(byte[] buffer) {
        try {
            ToolkitProtos.CalendarDefinition spec = ToolkitProtos.CalendarDefinition.parseFrom(buffer);
            return CalendarProtosUtility.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    public byte[] toBuffer(CalendarManager cntx) {
        return ModellingContextProto.convert(cntx).toByteArray();
    }

    public CalendarManager calendarsOf(byte[] buffer) {
        try {
            ToolkitProtos.Calendars cals = ToolkitProtos.Calendars.parseFrom(buffer);
            return ModellingContextProto.convert(cals);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    public byte[] toBuffer(TsDataSuppliers cntx) {
        return ModellingContextProto.convert(cntx).toByteArray();
    }

    public TsDataSuppliers variablesOf(byte[] buffer) {
        try {
            ToolkitProtos.TsDataSuppliers spec = ToolkitProtos.TsDataSuppliers.parseFrom(buffer);
            return ModellingContextProto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

}
