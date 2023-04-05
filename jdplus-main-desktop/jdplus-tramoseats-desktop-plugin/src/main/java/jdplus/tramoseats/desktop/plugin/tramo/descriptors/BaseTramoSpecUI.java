/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.tramoseats.desktop.plugin.tramo.descriptors;

import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.tramoseats.base.api.tramo.AutoModelSpec;
import jdplus.tramoseats.base.api.tramo.CalendarSpec;
import jdplus.tramoseats.base.api.tramo.EasterSpec;
import jdplus.tramoseats.base.api.tramo.EstimateSpec;
import jdplus.tramoseats.base.api.tramo.MeanSpec;
import jdplus.tramoseats.base.api.tramo.OutlierSpec;
import jdplus.tramoseats.base.api.tramo.RegressionSpec;
import jdplus.tramoseats.base.api.tramo.TradingDaysSpec;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.tramoseats.base.api.tramo.TransformSpec;

/**
 *
 * @author PALATEJ
 */
abstract class BaseTramoSpecUI implements IPropertyDescriptors{
    
    final TramoSpecRoot root;
        
    BaseTramoSpecUI(TramoSpecRoot root){
        this.root =root;
    }
    
    TramoSpec core(){return root.getCore();}
    
    boolean isRo(){return root.ro;}
    
    boolean isTransformationDefined(){
        return root.getCore().getTransform().getFunction() != TransformationType.Auto;
    }
    
    void update(MeanSpec spec) {
        update(root.core.getRegression()
                .toBuilder()
                .mean(spec)
                .build());
    }

    void update(EstimateSpec spec) {
        root.core = root.core.toBuilder().estimate(spec).build();
    }

    void update(SarimaSpec spec) {
        root.core = root.core.toBuilder().arima(spec).build();
    }

    void update(AutoModelSpec spec) {
        root.core = root.core.toBuilder().autoModel(spec).build();
    }

    void update(OutlierSpec spec) {
        root.core = root.core.toBuilder().outliers(spec).build();
    }

    void update(RegressionSpec spec) {
        root.core = root.core.toBuilder().regression(spec).build();
    }

    void update(TransformSpec spec) {
        root.core = root.core.toBuilder().transform(spec).build();
    }

    void update(CalendarSpec spec) {
        update(root.core.getRegression()
                .toBuilder()
                .calendar(spec)
                .build());
    }

    void update(EasterSpec spec) {
        update(root.core.getRegression()
                .getCalendar()
                .toBuilder()
                .easter(spec)
                .build());
    }

    void update(TradingDaysSpec spec) {
        update(root.core.getRegression()
                .getCalendar()
                .toBuilder()
                .tradingDays(spec)
                .build());
    }
}
