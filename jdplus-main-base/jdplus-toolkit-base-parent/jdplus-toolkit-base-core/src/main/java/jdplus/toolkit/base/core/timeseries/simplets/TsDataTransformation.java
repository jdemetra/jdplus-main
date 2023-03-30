/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.timeseries.simplets;

import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsObs;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.timeseries.TimeSeriesTransformation;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface TsDataTransformation extends TimeSeriesTransformation<TsPeriod, TsObs, TsData>{
    
    @Override
    TsDataTransformation converse();
}
