/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.timeseries.simplets;

import jdplus.toolkit.base.core.data.transformation.ConstTransformation;
import jdplus.toolkit.base.core.data.transformation.ExpTransformation;
import jdplus.toolkit.base.core.data.transformation.LogTransformation;
import jdplus.toolkit.base.api.data.OperationType;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Transformations {

    public TsDataTransformation log() {
        return new GenericTransformation(LogTransformation.EXEMPLAR);
    }

    public TsDataTransformation exp() {
        return new GenericTransformation(ExpTransformation.EXEMPLAR);
    }

    public TsDataTransformation lengthOfPeriod(LengthOfPeriodType lp) {
        return new LengthOfPeriodTransformation(lp);
    }
    
    public TsDataTransformation op(OperationType op, double factor) {
        return new GenericTransformation(new ConstTransformation(op, factor));
    }
    
    
}
