/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.arima.extensions;

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.arima.AutoCovarianceFunction;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class SarimaExtension extends InformationMapping<SarimaModel>{

    public SarimaExtension(){
        
        set("spectrum", double[].class, m->
        {
            double[] s=new double[361];
            DoubleUnaryOperator fn = m.getSpectrum().asFunction();
            for (int i=0; i<s.length; ++i){
                s[i]=fn.applyAsDouble(i*Math.PI/360);
            }
            return s;
        });
        set("ac", double[].class, m->
        {
            double[] ac=new double[37];
            AutoCovarianceFunction acf = m.stationaryTransformation().getStationaryModel().getAutoCovarianceFunction();
            for (int i=0; i<ac.length; ++i){
                ac[i]=acf.get(i);
            }
            return ac;
        });
    }
    
    @Override
    public Class<SarimaModel> getSourceClass() {
        return SarimaModel.class;
    }
    
}
