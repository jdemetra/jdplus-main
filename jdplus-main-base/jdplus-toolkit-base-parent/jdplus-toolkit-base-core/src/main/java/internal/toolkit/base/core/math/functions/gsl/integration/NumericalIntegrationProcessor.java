/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.toolkit.base.core.math.functions.gsl.integration;

import java.util.function.DoubleUnaryOperator;

import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(jdplus.toolkit.base.core.math.functions.NumericalIntegration.Processor.class)
public class NumericalIntegrationProcessor implements jdplus.toolkit.base.core.math.functions.NumericalIntegration.Processor {

    @Override
    public double integrate(DoubleUnaryOperator fn, double a, double b) {
        return NumericalIntegration.integrate(fn, a, b);
    }
}
