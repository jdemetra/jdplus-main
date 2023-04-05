/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.desktop.plugin.regarima.descriptors;

import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.x13.base.api.regarima.OutlierSpec;
import jdplus.x13.base.api.regarima.EstimateSpec;
import jdplus.x13.base.api.regarima.AutoModelSpec;
import jdplus.x13.base.api.regarima.BasicSpec;
import jdplus.x13.base.api.regarima.RegressionSpec;
import jdplus.x13.base.api.regarima.TransformSpec;
import jdplus.x13.base.api.regarima.EasterSpec;
import jdplus.x13.base.api.regarima.MeanSpec;
import jdplus.x13.base.api.regarima.TradingDaysSpec;
import jdplus.x13.base.api.regarima.RegArimaSpec;

/**
 *
 * @author PALATEJ
 */
abstract class BaseRegArimaSpecUI implements IPropertyDescriptors {

    final RegArimaSpecRoot root;

    BaseRegArimaSpecUI(RegArimaSpecRoot root) {
        this.root = root;
    }

    RegArimaSpec core() {
        return root.getCore();
    }

    boolean isRo() {
        return root.ro;
    }

    boolean isTransformationDefined(){
        return root.getCore().getTransform().getFunction() != TransformationType.Auto;
    }
    
    void update(BasicSpec spec) {
        root.core = root.core.toBuilder().basic(spec).build();
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

    void update(EasterSpec spec) {
        update(root.core.getRegression()
                .toBuilder()
                .easter(spec)
                .build());
    }

    void update(TradingDaysSpec spec) {
        update(root.core.getRegression()
                .toBuilder()
                .tradingDays(spec)
                .build());
    }
}
