package jdplus.toolkit.base.api.math.linearfilters;

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
@lombok.Builder(builderClassName="Builder", toBuilder=true)
public class LocalPolynomialFilterSpec implements FilterSpec{
    
    public static final LocalPolynomialFilterSpec DEF_TREND_SPEC=builder().build();
    public static final LocalPolynomialFilterSpec DEF_SEAS_SPEC=builder()
            .filterHorizon(2)
            .asymmetricPolynomialDegree(0)
            .linearModelCoefficients(Doubles.EMPTYARRAY)
            .build();

    /**
     * Horizon of the symmetric filter (defined in [-filterHorizon, +filterHorizon]
     * The full length of the filter is 1 + 2*filterHorizon
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
     * Type of the asymmetric filter used to compute the extremities of the series
     */
    private AsymmetricFilterOption asymmetricFilters;
    /**
     * Only used with MMSRE filters
     * Max degree of the polynomials preserved by the asymmetric filters 
     */
    private int asymmetricPolynomialDegree;
    /**
     * Only used with MMSRE filters
     * Coefficients of the extrapolating polynomial. See Luati-Proietti for more details
     */
    private double[] linearModelCoefficients;
    /**
     * Only used with MMSRE filters and with timeliness criterion 
     */
    private double timelinessWeight;
    /**
     * Only used with MMSRE filters and with timeliness criterion.
     * Upper threshold of the periods considered in the timeliness criterion (in radians)
     * Default is pi/8 (cycles of more than 4 years)
     */
    private double passBand;
    
    public static Builder builder(){
        return new Builder()
                .filterHorizon(6)
                .kernel(KernelOption.Henderson)
                .polynomialDegree(2)
                .asymmetricFilters(AsymmetricFilterOption.MMSRE)
                .asymmetricPolynomialDegree(1)
                .linearModelCoefficients(new double[]{2/(Math.sqrt(Math.PI)*3.5)})
                .timelinessWeight(0)
                .passBand(Math.PI/8);
                
    }
}
