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
package jdplus.tramoseats.base.core.seats;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.tramoseats.base.api.seats.SeatsException;
import jdplus.tramoseats.base.api.seats.SeatsModelSpec;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import nbbrd.design.Development;
import lombok.NonNull;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class SeatsKernel {

    public static final String SEATS = "seats";
    public static final String MODEL = "modelling", VALIDATION = "validation", DECOMPOSITION = "decomposition",
            ESTIMATION = "estimation", BIAS = "bias correction";

    private final SeatsToolkit toolkit;

    /**
     * @return the toolkit
     */
    public SeatsToolkit getToolkit() {
        return toolkit;
    }

    public SeatsKernel(@NonNull SeatsToolkit toolkit) {
        this.toolkit = toolkit;
    }

    public SeatsResults process(final SeatsModelSpec modelSpec, ProcessingLog log) {
        log.push(SEATS);
        try {
            // step 0. Build the model
            SeatsModel model = buildModel(modelSpec, log);
            // step 1. Validate the current model;
            validate(model, log);
            // step 2. Try to decompose the model
            decomposeModel(model, log);
            // step 3. Computation of the components
            estimateComponents(model, log);
            // step 4. Bias correction
            biasCorrection(model, log);
            return results(model);
        } finally {
            log.pop();
        }

    }

    private SeatsModel buildModel(SeatsModelSpec modelSpec, ProcessingLog log) {
        SeatsModel model = SeatsModel.of(modelSpec);
        model.setCurrentModel(model.getOriginalModel());
        return model;
    }

    private void validate(SeatsModel model, ProcessingLog log) {
        log.push(VALIDATION);
        try {
            IModelValidator validator = toolkit.getModelValidator();
            if (!validator.validate(model.getCurrentModel())) {
                model.setCurrentModel(validator.getNewModel());
                model.setParametersCutOff(true);
                log.remark(CUT_OFF);
            }
        } finally {
            log.pop();
        }
    }

    private final String NON_DECOMPOSABLE = "non decomposable model",
            CUT_OFF = "arima parameters cut off",
            APPROXIMATION = "model replaced by an approximation",
            NOISY = "noisy model used",
            SEATS_FAILED = "canonical decomposition failed";

    private void decomposeModel(SeatsModel model, ProcessingLog log) {
        log.push(DECOMPOSITION);
        try {
            IModelApproximator approximator = toolkit.getModelApproximator();
            IModelDecomposer decomposer = toolkit.getModelDecomposer();
            UcarimaModel ucm = null;
            int nround = 0;
            while (++nround <= 10) {
                ucm = decomposer.decompose(model.getCurrentModel());
                if (ucm == null && nround == 1) {
                    log.warning(NON_DECOMPOSABLE);
                }
                if (ucm != null || approximator == null) {
                    break;
                }
                if (!approximator.approximate(model)) {
                    break;
                } else {
                    model.setModelChanged(true);
                    log.remark(APPROXIMATION, model.getCurrentModel().orders());
                }
            }
            if (ucm == null) {
                    log.error(SEATS_FAILED);
               throw new SeatsException(SeatsException.ERR_DECOMP);
            }
            if (!ucm.getModel().equals(model.getCurrentModel())) {
                model.setModelChanged(true);
                log.warning(NOISY);
            }
            model.setUcarimaModel(ucm);
        } finally {
            log.pop();
        }
    }

    private void estimateComponents(SeatsModel model, ProcessingLog log) {
        IComponentsEstimator componentsEstimator = toolkit.getComponentsEstimator();
        model.setInitialComponents(componentsEstimator.decompose(model));
    }

    private void biasCorrection(SeatsModel model, ProcessingLog log) {
        IBiasCorrector bias = toolkit.getBiasCorrector();
        if (bias != null) {
            bias.correctBias(model);
        }
    }

    private SeatsResults results(SeatsModel model) {
        return SeatsResults.builder()
                .originalModel(model.getOriginalModel())
                .finalModel(model.getCurrentModel())
                .meanCorrection(model.isMeanCorrection())
                .innovationVariance(model.getInnovationVariance())
                .parametersCutOff(model.isParametersCutOff())
                .modelChanged(model.isModelChanged())
                .ucarimaModel(model.getUcarimaModel())
                .compactUcarimaModel(model.compactUcarimaModel(false, false))
                .initialComponents(model.getInitialComponents())
                .finalComponents(model.getFinalComponents())
                .build();
    }
}
