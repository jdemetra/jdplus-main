/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.timeseries.simplets.analysis;

import jdplus.toolkit.base.api.timeseries.TsData;

/**
 * Returns a value, for a given time series and a given position in that time series
 * @author Jean Palate <jean.palate@nbb.be>
 */
@FunctionalInterface
public interface TsDataFunction {
    double apply(TsData s, int pos);
}
