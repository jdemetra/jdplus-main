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
package jdplus.toolkit.base.core.math.linearfilters;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.linearfilters.HendersonSpec;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;


/**
 *
 * @author palatej
 */
public class AsymmetricFiltersFactoryTest {
    
    public AsymmetricFiltersFactoryTest() {
    }

    public static void main(String[] args) {
        SymmetricFilter h13 = HendersonFilters.ofLength(23);
        IFiniteFilter[] ff = AsymmetricFiltersFactory.musgraveFilters(h13, 4.5);
        for (int i=0; i<ff.length; ++i){
            System.out.println(DoubleSeq.of(ff[i].weightsToArray()));
        }
        
        IFiniteFilter[] lf=new IFiniteFilter[ff.length];
        for (int i=0; i<lf.length; ++i){
            lf[i]=ff[i].mirror();
        }
        
        System.out.println();
        
        Filtering f=new Filtering(h13, lf, ff);
        DoubleSeq s=DoubleSeq.of(Data.NILE);
        DoubleSeq st = f.process(s);
        System.out.println(s);
        System.out.println(st);
        
        HendersonSpec spec=new HendersonSpec(11, 4.5);
        IQuasiSymmetricFiltering sf = HendersonFilters.of(spec);
        st = sf.process(s);
        System.out.println(st);
   }
    
}
