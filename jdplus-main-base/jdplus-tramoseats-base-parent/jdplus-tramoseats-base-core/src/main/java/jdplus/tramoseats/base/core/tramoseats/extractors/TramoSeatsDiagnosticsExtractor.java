/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramoseats.extractors;

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.StationaryVarianceDecomposition;
import jdplus.sa.base.core.diagnostics.GenericSaTests;
import jdplus.tramoseats.base.core.seats.SeatsTests;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDiagnostics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class TramoSeatsDiagnosticsExtractor extends InformationMapping<TramoSeatsDiagnostics> {
    
 
 public TramoSeatsDiagnosticsExtractor(){
        delegate("diagnostics", GenericSaTests.class, source -> source.getGenericDiagnostics());

        delegate(SaDictionaries.VARIANCE, StationaryVarianceDecomposition.class, source -> source.getVarianceDecomposition());
        
        delegate("seats", SeatsTests.class, source -> source.getSpecificDiagnostics());
    }

    @Override
    public Class<TramoSeatsDiagnostics> getSourceClass() {
        return TramoSeatsDiagnostics.class;
    }
    
}
