/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.base.core.modelling;

import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.util.function.Predicate;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegArimaDecomposer {

    /**
     * Gets all the deterministic effect corresponding to a given component
     * type.It includes the pre-specified (fixed) regression effects and the
     * estimated
     * regression effects
     *
     * @param <M>
     * @param model
     * @param domain The requested time domain
     * @param type The type of the component
     * @param transformed If true, the deterministic effect corresponds to the
     * model
     * after transformation (log and length of period). If false, the estimated
     * deterministic effects are back-transformed (exp). Only the regression
     * effect
     * corresponding to the calendar effect will be "back-corrected" for
     * length of period pre-adjustment.
     * @return
     */
    public <M> TsData deterministicEffect(GeneralLinearModel<M> model, TsDomain domain, ComponentType type, boolean transformed) {
        TsData f = model.deterministicEffect(domain, v ->  SaVariable.isRegressionEffect(v, type) );
        if (f != null && !transformed) {
            f = model.backTransform(f, type == ComponentType.CalendarEffect);
        }
        return f;
    }

    /**
     * Gets all the deterministic effect corresponding to a given component
     * type and verifying the given test.It includes the pre-specified
 (fixed) regression effects and the estimated regression effects
     *
     * @param <M>
     * @param model
     * @param domain The requested time domain
     * @param type The type of the component
     * @param transformed If true, the deterministic effect corresponds to the
     * model after transformation (log only). If false, the estimated
     * deterministic effects are back-transformed (exp). The series is never
     * corrected for length of period pre-adjustment.
     * @param test The selection test
     * @return
     */
    public <M> TsData deterministicEffect(GeneralLinearModel<M> model, TsDomain domain, ComponentType type, boolean transformed, Predicate<Variable> test) {
        TsData f = model.deterministicEffect(domain, v -> test.test(v) && SaVariable.isRegressionEffect(v, type));
        if (f != null && !transformed) {
            f = model.backTransform(f, false);
        }
        return f;
    }


}
