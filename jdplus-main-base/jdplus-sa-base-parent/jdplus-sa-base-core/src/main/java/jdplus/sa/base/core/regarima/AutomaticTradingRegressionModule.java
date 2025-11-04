/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.sa.base.core.regarima;

import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionModule;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;

/**
 *
 * @author Jean Palate
 */
public interface AutomaticTradingRegressionModule extends IRegressionModule{
    public static final String ATD = "automatic trading days selection", 
            TD_SEL = "selected trading days: ",
            ATD_FAILED = "automatic trading days selection failed";

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class Info {

        final String[] names;
        final LikelihoodStatistics[] ll;
        final int best;
    }

    public static String[] modelNames(ITradingDaysVariable[] td){
        String[] names= new String[2+ td.length];
        names[0]="no td";
        names[1]="lp";
        for (int i=0; i<td.length; ++i){
            names[i+2]=td[i].description(null);
        }
        return names;
    }

    
}
