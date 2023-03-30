/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramo;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.core.regsarima.regular.IModelBuilder;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.api.timeseries.TsData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
class DefaultModelBuilder implements IModelBuilder {

    public DefaultModelBuilder() {
    }

    @Override
    public ModelDescription build(TsData series, ProcessingLog log) {
        ModelDescription model=new ModelDescription(series, null);
        model.setAirline(series.getAnnualFrequency()>1);
        model.setMean(true);
        return model;
    }
    
}
