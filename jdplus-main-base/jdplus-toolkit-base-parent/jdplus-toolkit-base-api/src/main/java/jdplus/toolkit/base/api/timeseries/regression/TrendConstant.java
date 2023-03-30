/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.api.timeseries.regression;

import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import java.time.LocalDateTime;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@Development(status=Development.Status.Release)
public class TrendConstant implements ISystemVariable {
    
    public static final String NAME="const";
    
    private int d, bd;
    private LocalDateTime reference;

    @Override
    public int dim() {
        return 1;
    }

     @Override
    public <D extends TimeSeriesDomain<?>> String description(D context){
        return "Constant";
    }
   
    public TrendConstant(int d, int bd){
        this.d=d;
        this.bd=bd;
        this.reference=null;
    }
}
