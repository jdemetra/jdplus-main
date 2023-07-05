package jdplus.toolkit.base.core.math.linearfilters.advanced.dfa;

import jdplus.toolkit.base.core.math.linearfilters.advanced.MSEDecomposition;
import java.util.function.DoubleUnaryOperator;

import jdplus.toolkit.base.core.math.linearfilters.advanced.SpectralDensity;
import jdplus.toolkit.base.core.math.linearfilters.advanced.IFiltering;
import jdplus.toolkit.base.core.math.linearfilters.advanced.ISymmetricFiltering;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.linearfilters.advanced.LocalPolynomialFiltersFactory;
import jdplus.toolkit.base.core.math.linearfilters.advanced.LocalPolynomialFilterSpec;
import org.junit.jupiter.api.Test;

public class DFAFilterTest {
	
	public DFAFilterTest() {
    }

    @Test
	 public void testSymmetric() {
    	int len = 6;
    	LocalPolynomialFilterSpec spec = new LocalPolynomialFilterSpec();
    	spec.setFilterLength(len);
        IFiltering lf = LocalPolynomialFiltersFactory.of(spec);
        SymmetricFilter sf = ((ISymmetricFiltering) lf).symmetricFilter();
    	DFAFilter ff = DFAFilter.builder()
                .nlags(len)
                .nleads(len)
                .symetricFilter(sf)
                .density(SpectralDensity.WhiteNoise.asFunction())
                .build();
        DFAFilter.Results rslt = ff.make(0.33, 0.33, 0.33);
//        System.out.println(DoubleSeq.of(rslt.getFilter().weightsToArray()));
        DoubleUnaryOperator sd = x -> 1;
        MSEDecomposition.of(sd, sf.frequencyResponseFunction(), rslt.getFilter().frequencyResponseFunction(), Math.PI/12);
        MSEDecomposition d = MSEDecomposition.of(sd, sf.frequencyResponseFunction(), rslt.getFilter().frequencyResponseFunction(), Math.PI/12);
 //       System.out.println(DoubleSeq.of(d.getAccuracy(), d.getSmoothness(), d.getTimeliness(), d.getResidual(), d.getTotal()));
        }
}
