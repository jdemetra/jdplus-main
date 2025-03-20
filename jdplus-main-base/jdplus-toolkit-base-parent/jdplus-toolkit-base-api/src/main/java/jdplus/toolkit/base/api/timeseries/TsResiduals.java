/*
 * Copyright 2024 JDemetra+.
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
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.regression.ResidualsType;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class TsResiduals {
     
    @lombok.NonNull
    private ResidualsType type;
    
    @lombok.NonNull 
    private DoubleSeq res;
    
    private TsData tsres;
    
    private double ssq;
    
    private int n, df, dfc;
    
    @lombok.Singular
    private Map<String, StatisticalTest> tests;
   
}
