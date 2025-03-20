/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.regsarima.regular;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Getter
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
public class RegSarimaModelling {

    @NonNull
    private ModelDescription description;
    private RegArimaEstimation<SarimaModel> estimation;

    @NonNull
    private final ProcessingLog log;
    
    public static RegSarimaModelling of(ModelDescription desc){
        return new RegSarimaModelling(desc, null, ProcessingLog.dummy());
    }

    public static RegSarimaModelling of(ModelDescription desc, ProcessingLog log){
        return new RegSarimaModelling(desc, null, log);
    }

    public static RegSarimaModelling copyOf(RegSarimaModelling modelling){
        return new RegSarimaModelling(ModelDescription.copyOf(modelling.description),
        modelling.estimation, ProcessingLog.dummy());
    }

    public void estimate(IRegArimaComputer<SarimaModel> processor) {
        estimation = description.estimate(processor);
    }

    public void estimate(double precision) {
        RegSarimaComputer processor = RegSarimaComputer.builder()
                .precision(precision)
                .startingPoint(RegSarimaComputer.StartingPoint.HannanRissanen)
                .build();

        estimation = description.estimate(processor);
    }

    public RegSarimaModel build() {
        return RegSarimaModel.of(description, estimation, log);
    }
    
    public void clearEstimation(){
        estimation=null;
        description.freeArimaParameters();
    }

    public boolean needEstimation() {
        return estimation == null;
    }
    
    /**
     * Change the Arima specification and remove the current estimation
     * @param spec 
     */
    public void setSpecification(SarimaOrders spec){
        description.setSpecification(spec);
        this.estimation=null;
    }

    public void setDescription(ModelDescription desc){
        this.description=desc;
        this.estimation=null;
    }
    
    public void set(ModelDescription desc, RegArimaEstimation<SarimaModel> est){
        this.description=desc;
        this.estimation=est;
    }
   
   
//    public ModellingContext() {
//        processingLog = new ArrayList<>();
//    }
//
//    public ModellingContext(boolean log) {
//        if (log) {
//            processingLog = new ArrayList<>();
//        } else {
//            processingLog = null;
//        }
//    }
//
//    public PreprocessingModel tmpModel() {
//        return new PreprocessingModel(description, estimation, log);
//    }

//    public PreprocessingModel current(boolean update) {
//        if (!update) {
//            PreprocessingModel model = new PreprocessingModel(description.clone(), estimation);
//            model.info_ = information.clone();
//            return model;
//
//        } else {
//            PreprocessingModel model = new PreprocessingModel(description.clone(), estimation);
//            model.updateModel();
//            model.info_ = information.clone();
//            return model;
//        }
//    }
//    public final InformationSet information = new InformationSet();
//    @Deprecated
//    public boolean verbose = false;
//    public final List<ProcessingInformation> processingLog;
//    public boolean automodelling = false;
//    public boolean outliers = false;
//    public boolean hasseas;
//    public int originalSeasonalityTest;
}
