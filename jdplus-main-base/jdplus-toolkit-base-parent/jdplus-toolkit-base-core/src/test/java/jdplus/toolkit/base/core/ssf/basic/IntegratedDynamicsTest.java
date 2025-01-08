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
package jdplus.toolkit.base.core.ssf.basic;

import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.utility.DynamicsCoherence;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class IntegratedDynamicsTest {
    
    public IntegratedDynamicsTest() {
    }

    @Test
    public void testDynamics(){
        SarmaOrders spec = new SarmaOrders(12);
        spec.setQ(1);
        spec.setBq(1);
        IArimaModel arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        StateComponent cmp = SsfArima.stateComponent(arima);
        IntegratedDynamics dyn=new IntegratedDynamics(cmp.dynamics(), SsfArima.defaultLoading(), DoubleSeq.of(-2,1));
        DynamicsCoherence.check(dyn, cmp.dim()+2);
    }
    
}
