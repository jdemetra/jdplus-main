/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.toolkit.base.core.regsarima.regular;

import jdplus.toolkit.base.api.arima.SarmaOrders;

/**
 * Estimates the stationary part of the arima model provided in the context
 * The context should be contain the new specification on exit. The fully estimated model
 * is not necessary provided
 * @author Jean Palate
 */
public interface IArmaModule {
    
    public static final String ARMA = "arma selection",
            MODEL = "selected model: ", DEFAULT = "default model selected (not enough obs.)",
            FAILED = "arma selection failed";

    public static interface Info{
        SarmaOrders bestModel();
        SarmaOrders[] models();
        double[] bic();
    }
    
    ProcessingResult process(RegSarimaModelling context);
    
}
