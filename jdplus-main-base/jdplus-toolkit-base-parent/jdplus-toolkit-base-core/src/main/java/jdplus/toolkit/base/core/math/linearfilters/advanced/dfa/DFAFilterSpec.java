package jdplus.toolkit.base.core.math.linearfilters.advanced.dfa;

import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.linearfilters.advanced.SpectralDensity;

@lombok.Data
public class DFAFilterSpec {
    private double w0 = 0, w1 = Math.PI / 8;
    private double accuracyWeight =1/3 , smoothnessWeight = 1/3, timelinessWeight = 1/3;
    //private double residualWeight = 0;
    private int lags = 6, leads = 0;
    private int polynomialPreservationDegree = 0;
    private SpectralDensity density=SpectralDensity.WhiteNoise;
    private SymmetricFilter target;
}
