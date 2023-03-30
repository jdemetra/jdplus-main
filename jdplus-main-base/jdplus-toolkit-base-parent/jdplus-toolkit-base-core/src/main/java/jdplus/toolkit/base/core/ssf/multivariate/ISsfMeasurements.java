/*
 * Copyright 2015 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.multivariate;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.ISsfRoot;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public interface ISsfMeasurements extends ISsfRoot {

//<editor-fold defaultstate="collapsed" desc="description">
    /**
     * Gets the number of measurements at a given position
     *
      * @return The number of measurements. 
     */
    int getCount();

    /**
     * Gets the loading at a given position for a given measurement
     * @param equation The considered equation. Should belong to [0, getCount(pos)]
     * @return 
     */
    ISsfLoading loading(int equation);
    
    ISsfErrors errors();

//</editor-fold>

    default void ZM(int pos, FastMatrix L, FastMatrix ZL){
        DataBlockIterator rows = ZL.rowsIterator();
        int r=0;
        while (rows.hasNext())
            loading(r++).ZM(pos, L, rows.next());
    }

    default void ZX(int pos, DataBlock X, DataBlock zx){
        int n=getCount();
        for (int i=0; i<n; ++i){
            zx.set(i, loading(i).ZX(pos, X));
        }
    }
}
