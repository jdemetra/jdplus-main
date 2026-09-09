/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf.sts;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 *
 * @author palatej
 */
public class SplineComponentTest {
    
    public SplineComponentTest() {
    }

    @Test
    public void testMonthly() {
        RegularSplineComponent.Data sd=RegularSplineComponent.Data.of(new int[]{0,4,5,6,7,12});
        CompositeSsf ssf = CompositeSsf.builder()
                .add(LocalLinearTrend.stateComponent(0.1, 0.1), LocalLinearTrend.defaultLoading())
                .add(Noise.of(1), Noise.defaultLoading())
                .add(RegularSplineComponent.stateComponent(sd,1), RegularSplineComponent.loading(sd, 0))
                .build();
        
        DefaultSmoothingResults rslt = AkfToolkit.smooth(ssf, new SsfData(Data.PROD), true, true, true);
        System.out.println(rslt.getComponent(ssf.componentsPosition()[0]));
        System.out.println(rslt.getComponent(ssf.componentsPosition()[1]));
        System.out.println(rslt.getComponentVariance(ssf.componentsPosition()[0]));
        
        ISsfLoading loading = RegularSplineComponent.loading(sd, 0);
        for (int i=0; i<rslt.size(); ++i){
            double z=loading.ZX(i, rslt.a(i).extract(ssf.componentsPosition()[2], ssf.componentsDimension()[2]));
            System.out.print(z);
            System.out.print('\t');
        }
        
     }
    
}
