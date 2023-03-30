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
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.ModifiedTsVariable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
class ModifiedTsVariableFactory implements RegressionVariableFactory<ModifiedTsVariable> {

    static ModifiedTsVariableFactory FACTORY = new ModifiedTsVariableFactory();

    private ModifiedTsVariableFactory() {
    }

    @Override
    public boolean fill(ModifiedTsVariable var, TsPeriod start, FastMatrix buffer) {
        ITsVariable core = var.getVariable();
        List<ModifiedTsVariable.Modifier> modifiers = var.getModifiers();
        // Computes first the necessary domains
        List<TsDomain> doms = new ArrayList<>();
        TsDomain cur = TsDomain.of(start, buffer.getRowsCount());
        for (ModifiedTsVariable.Modifier modifier : modifiers) {
            doms.add(cur);
            ModifierFactory factory = Modifiers.factoryFor(modifier);
            if (factory == null) {
                return false;
            }
            cur = factory.needFor(modifier, cur);
        }

        int dim = core.dim();
        FastMatrix input = FastMatrix.make(cur.getLength(), dim);
        RegressionVariableFactory vfactory = Regression.factoryFor(core);
        if (vfactory == null) {
            return false;
        }
        vfactory.fill(core, cur.getStartPeriod(), input);
        FastMatrix output;
        for (int i = modifiers.size() - 1; i > 0; --i) {
            ModifiedTsVariable.Modifier modifier = modifiers.get(i);
            TsDomain dom = doms.get(i);
            dim = modifier.redim(dim);
            output = FastMatrix.make(dom.getLength(), dim);
            ModifierFactory factory = Modifiers.factoryFor(modifier);
            if (! factory.fill(modifier, dom.getStartPeriod(), input, output))
                return false;
            input = output;
        }

        ModifiedTsVariable.Modifier modifier = modifiers.get(0);
        ModifierFactory factory = Modifiers.factoryFor(modifier);
        factory.fill(modifier, start, input, buffer);

        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(ModifiedTsVariable var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
