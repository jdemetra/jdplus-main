/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.functions;

import jdplus.toolkit.base.api.design.Algorithm;
import jdplus.toolkit.base.api.design.InterchangeableProcessor;
import nbbrd.design.Development;
import internal.toolkit.base.core.math.functions.gsl.integration.NumericalIntegrationProcessor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleUnaryOperator;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class NumericalIntegration {

    private final AtomicReference<Processor> PROCESSOR = new AtomicReference<>(NumericalIntegrationLoader.Processor.load());

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public double integrate(DoubleUnaryOperator fn, double a, double b) {
        return PROCESSOR.get().integrate(fn, a, b);
    }

    @InterchangeableProcessor
    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE
      , fallback = NumericalIntegrationProcessor.class)
    @FunctionalInterface
    public static interface Processor {

        double integrate(DoubleUnaryOperator fn, double a, double b);
    }

}
