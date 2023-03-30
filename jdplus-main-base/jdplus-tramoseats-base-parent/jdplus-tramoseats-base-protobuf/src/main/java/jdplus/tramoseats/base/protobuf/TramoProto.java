/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.protobuf;

import jdplus.toolkit.base.protobuf.regarima.RegArimaEstimationProto;
import jdplus.toolkit.base.protobuf.regarima.RegArimaProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoProto {

    public TramoSpec convert(jdplus.tramoseats.base.api.tramo.TramoSpec spec) {
        return TramoSpec.newBuilder()
                .setBasic(BasicProto.convert(spec.getTransform()))
                .setTransform(TransformProto.convert(spec.getTransform()))
                .setOutlier(OutlierProto.convert(spec.getOutliers()))
                .setArima(RegArimaProtosUtility.convert(spec.getArima()))
                .setAutomodel(AutoModelProto.convert(spec.getAutoModel()))
                .setRegression(RegressionProto.convert(spec.getRegression()))
                .setEstimate(EstimateProto.convert(spec.getEstimate()))
                .build();
    }

    public jdplus.tramoseats.base.api.tramo.TramoSpec convert(TramoSpec spec) {
        return jdplus.tramoseats.base.api.tramo.TramoSpec.builder()
                .transform(TransformProto.convert(spec.getBasic(), spec.getTransform()))
                .outliers(OutlierProto.convert(spec.getOutlier()))
                .arima(RegArimaProtosUtility.convert(spec.getArima()))
                .autoModel(AutoModelProto.convert(spec.getAutomodel()))
                .regression(RegressionProto.convert(spec.getRegression(), spec.getOutlier().getTcrate()))
                .estimate(EstimateProto.convert(spec.getEstimate()))
                .build();
    }
    
        public TramoOutput convert(jdplus.tramoseats.base.core.tramo.TramoOutput output){
        TramoOutput.Builder builder = 
                TramoOutput.newBuilder()
                .setEstimationSpec(TramoProto.convert(output.getEstimationSpec()));
        
        if (output.getResult() != null){
            builder.setResult(RegArimaEstimationProto.convert(output.getResult()))
                    .setResultSpec(TramoProto.convert(output.getResultSpec()));
        }
        // TODO detail and logs
        
        return builder.build();
    }

}
