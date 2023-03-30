/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.api.timeseries.regression;

import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsMoniker;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class DynamicTsVariable implements IUserVariable {

    @lombok.NonNull
    private final TsMoniker moniker;
    @lombok.NonNull
    private final String description;
    
    private final TsData data;
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context){
        return description;
    }
 
    @Override
    public int dim() {
        return 1;
    }

}
