/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.polynomials;

import jdplus.toolkit.base.core.math.polynomials.JacobiPolynomials;

/**
 *
 * @author Jean Palate
 */
public class JacobiPolynomialsTest {
    
    public JacobiPolynomialsTest() {
    }

//    @Test
    public void testLegendre() {
        for (int i=0; i<20; ++i){
            System.out.println(JacobiPolynomials.jacobi(i, 0, 0));
        }
    }
    
}
