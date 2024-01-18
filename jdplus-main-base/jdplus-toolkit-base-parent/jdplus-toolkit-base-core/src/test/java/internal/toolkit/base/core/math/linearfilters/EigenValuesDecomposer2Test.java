package internal.toolkit.base.core.math.linearfilters;

import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.math.polynomials.UnitRoots;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EigenValuesDecomposer2Test {

    @Test
    void testUnitRoots(){
        Polynomial p = UnitRoots.D(1, 3).times(UnitRoots.S(64, 2));      
        SymmetricFilter sf = SymmetricFilter.convolutionOf(new BackFilter(p));
        EigenValuesDecomposer2 decomp=new EigenValuesDecomposer2();
        decomp.decompose(sf);
        double norm2 = decomp.getBFilter().asPolynomial().minus(p).coefficients().norm2();
        assertTrue(norm2<1e-6);
    }
    
    @Test
    void test1(){
        Polynomial p = UnitRoots.D(1).times(UnitRoots.S(12,1));    
        p=p.times(Polynomial.of(1, -.2, -.5, .124, 1/333.0));
        SymmetricFilter sf = SymmetricFilter.convolutionOf(new BackFilter(p));
        EigenValuesDecomposer2 decomp=new EigenValuesDecomposer2();
        decomp.decompose(sf);
        double norm2 = decomp.getBFilter().asPolynomial().minus(p).coefficients().norm2();
        System.out.println(p);
        assertTrue(norm2<1e-6);
    }
}