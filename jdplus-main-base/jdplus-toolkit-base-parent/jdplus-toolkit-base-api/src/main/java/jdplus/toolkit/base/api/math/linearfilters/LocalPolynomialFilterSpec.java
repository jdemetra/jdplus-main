package jdplus.toolkit.base.api.math.linearfilters;

import java.util.Arrays;
import jdplus.toolkit.base.api.data.Doubles;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class LocalPolynomialFilterSpec implements FilterSpec {

    public static final double DEF_SLOPE = 2 / (Math.sqrt(Math.PI) * 3.5);

    public static final LocalPolynomialFilterSpec DEF_TREND_SPEC = builder().build();
    public static final LocalPolynomialFilterSpec DEF_SEAS_SPEC = builder()
            .filterHorizon(2)
            .polynomialDegree(0)
            .asymmetricPolynomialDegree(0)
            .linearModelCoefficients(Doubles.EMPTYARRAY)
            .build();

    public static LocalPolynomialFilterSpec defaultSeasonalSpec(int horizon, KernelOption kernel) {
        return builder()
                .filterHorizon(horizon)
                .kernel(kernel)
                .polynomialDegree(0)
                .asymmetricPolynomialDegree(0)
                .linearModelCoefficients(Doubles.EMPTYARRAY)
                .build();
    }

    /**
     * Horizon of the symmetric filter (defined in [-filterHorizon,
     * +filterHorizon] The full length of the filter is 1 + 2*filterHorizon
     */
    private int filterHorizon;
    /**
     * Defines the kernel used in the filter
     */
    private KernelOption kernel;
    /**
     * Degree of the local polynomial
     */
    private int polynomialDegree;
    /**
     * Type of the asymmetric filter used to compute the extremities of the
     * series
     */
    private AsymmetricFilterOption asymmetricFilters;
    /**
     * Only used with MMSRE filters Max degree of the polynomials preserved by
     * the asymmetric filters
     */
    private int asymmetricPolynomialDegree;
    /**
     * Only used with MMSRE filters Coefficients of the extrapolating
     * polynomial. See Luati-Proietti for more details
     */
    private double[] leftLinearModelCoefficients;
    /**
     * Only used with MMSRE filters Coefficients of the extrapolating
     * polynomial. See Luati-Proietti for more details
     */
    private double[] rightLinearModelCoefficients;
    /**
     * Only used with MMSRE filters and with timeliness criterion
     */
    private double timelinessWeight;
    /**
     * Only used with MMSRE filters and with timeliness criterion. Upper
     * threshold of the periods considered in the timeliness criterion (in
     * radians) Default is pi/8 (cycles of more than 4 years)
     */
    private double passBand;

    public static Builder builder() {
        return new Builder()
                .filterHorizon(6)
                .kernel(KernelOption.Henderson)
                .polynomialDegree(2)
                .asymmetricFilters(AsymmetricFilterOption.MMSRE)
                .asymmetricPolynomialDegree(0)
                .linearModelCoefficients(DEF_SLOPE)
                .timelinessWeight(0)
                .passBand(Math.PI / 8);

    }

    public static class Builder {

        public Builder linearModelCoefficients(double... coefs) {
            leftLinearModelCoefficients = coefs;
            rightLinearModelCoefficients = coefs;
            return this;
        }

        public Builder leftLinearModelCoefficients(double... coefs) {
            leftLinearModelCoefficients = coefs;
            return this;
        }

        public Builder rightLinearModelCoefficients(double... coefs) {
            rightLinearModelCoefficients = coefs;
            return this;
        }
    }

    public boolean isSymmetric() {
        return asymmetricFilters == AsymmetricFilterOption.CutAndNormalize
                || asymmetricFilters == AsymmetricFilterOption.Direct
                || Arrays.equals(leftLinearModelCoefficients, rightLinearModelCoefficients);
    }

}
