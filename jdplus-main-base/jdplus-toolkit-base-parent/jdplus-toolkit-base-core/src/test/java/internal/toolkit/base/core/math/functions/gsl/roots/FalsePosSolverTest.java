/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.toolkit.base.core.math.functions.gsl.roots;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FalsePosSolverTest {

    @Test
    public void test() {
        SolverTestUtils.testAll("False position", FalsePosSolver::new);
    }
}
