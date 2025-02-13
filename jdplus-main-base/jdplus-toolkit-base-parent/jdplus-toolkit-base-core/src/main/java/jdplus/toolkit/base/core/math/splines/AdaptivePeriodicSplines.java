/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.base.core.math.splines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class AdaptivePeriodicSplines {

    public static enum Criterion {
        AIC, BIC, EBIC
    }

    @lombok.Value
    @lombok.Builder(builderClassName = "Builder")
    public static class Specification {

        public static final Specification DEF_SPEC = builder().build();

        double lambda0, lambda1, lambdaStep;
        int minKnots;
        Criterion criterion;

        public static Builder builder() {
            return new Builder()
                    .minKnots(0)
                    .lambda0(0)
                    .lambda1(10)
                    .lambdaStep(0.025)
                    .criterion(Criterion.BIC);
        }
    }

    final Specification spec;
    AdaptivePeriodicSpline aspline;
    final List<AdaptivePeriodicSpline.Step> steps = new ArrayList<>();
    int best;

    public AdaptivePeriodicSplines() {
        this.spec = Specification.DEF_SPEC;
    }

    public AdaptivePeriodicSplines(Specification spec) {
        this.spec = spec;
    }

    public void process(AdaptivePeriodicSpline aspline) {
        this.aspline = aspline;
        this.steps.clear();
        double ll0 = 0;
        double min = 0;
        best = -1;
        int cur = 0;
        for (double lambda = spec.getLambda0(); lambda <= spec.getLambda1(); lambda += spec.getLambdaStep()) {
            aspline.process(lambda);
            AdaptivePeriodicSpline.Step result = aspline.result();
            if (result == null) {
                break;
            }
            //w=result.getW().clone();
            int nsel = AdaptivePeriodicSpline.Step.selectedKnotsCount(result.getZ(), aspline.getSpecification());
            if (nsel < spec.minKnots || nsel < aspline.getSpecification().getFixedKnotsCount()) {
                break;
            }
            double ll = result.getLl();
            if (ll0 == 0 || steps.stream().allMatch(s -> s.getLl() != ll)) {
                steps.add(result);
                double c = 0;
                switch (spec.criterion) {
                    case AIC ->
                        c = result.getAic();
                    case BIC ->
                        c = result.getBic();
                    case EBIC ->
                        c = result.getEbic();
                }
                if (min == 0 || c < min) {
                    min = c;
                    best = cur;
                }
                ll0 = result.getLl();
                ++cur;
            }
        }
    }

    public int best() {
        return best;
    }

    public int resultsSize() {
        return steps.size();
    }

    public FastMatrix A() {
        FastMatrix A = FastMatrix.make(steps.size(), aspline.getSpecification().getKnots().length);
        DataBlockIterator rows = A.rowsIterator();
        for (AdaptivePeriodicSpline.Step step : steps) {
            rows.next().copy(DataBlock.of(step.getA()));
        }
        return A;
    }

    public FastMatrix Z() {
        FastMatrix Z = FastMatrix.make(steps.size(), aspline.getSpecification().getKnots().length);
        DataBlockIterator rows = Z.rowsIterator();
        for (AdaptivePeriodicSpline.Step step : steps) {
            rows.next().copy(DataBlock.of(step.getZ()));
        }
        return Z;
    }

    public FastMatrix W() {
        FastMatrix W = FastMatrix.make(steps.size(), aspline.getSpecification().getKnots().length);
        DataBlockIterator rows = W.rowsIterator();
        for (AdaptivePeriodicSpline.Step step : steps) {
            rows.next().copy(DataBlock.of(step.getW()));
        }
        return W;
    }

    public FastMatrix S() {
        FastMatrix S = FastMatrix.make(steps.size(), aspline.getSpecification().getKnots().length);
        DataBlockIterator rows = S.rowsIterator();
        for (AdaptivePeriodicSpline.Step step : steps) {
            rows.next().copy(DataBlock.of(step.getS()));
        }
        return S;
    }

    public AdaptivePeriodicSpline.Step result(int i) {
        return steps.get(i);
    }

    public List<AdaptivePeriodicSpline.Step> allResults() {
        return Collections.unmodifiableList(steps);
    }

    public AdaptivePeriodicSpline.Step result() {
        return steps.get(steps.size() - 1);
    }

    public AdaptivePeriodicSpline adaptiveSpline() {
        return aspline;
    }

    public int selectedKnotsCount() {
        if (best < 0) {
            throw new RuntimeException("Processing not done");
        }
        return selectedKnotsCount(best);
    }

    public int[] selectedKnotsPosition() {
        if (best < 0) {
            throw new RuntimeException("Processing not done");
        }
        return selectedKnotsPosition(best);
    }

    public double[] selectedKnots() {
        if (best < 0) {
            throw new RuntimeException("Processing not done");
        }
        return selectedKnots(best);
    }

    public int selectedKnotsCount(int pos) {
        double[] z = steps.get(pos).getZ();
        if (z == null) {
            return aspline.getSpecification().getKnots().length;
        }
        return AdaptivePeriodicSpline.Step.selectedKnotsCount(z, aspline.getSpecification());
    }

    public int[] selectedKnotsPosition(int pos) {
        double[] z = steps.get(pos).getZ();
        if (z == null) {
            return null;
        }
        return AdaptivePeriodicSpline.Step.selectedKnotsPosition(z, aspline.getSpecification());
    }

    public double[] selectedKnots(int pos) {
        double[] z = steps.get(pos).getZ();
        if (z == null) {
            return null;
        }
        return AdaptivePeriodicSpline.Step.selectedKnots(z, aspline.getSpecification());
    }

}
