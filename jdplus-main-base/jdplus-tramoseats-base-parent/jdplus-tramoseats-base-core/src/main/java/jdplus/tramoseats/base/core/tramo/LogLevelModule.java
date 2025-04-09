/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tramoseats.base.core.tramo;

import java.util.Formatter;
import java.util.Locale;
import jdplus.tramoseats.base.core.tramo.internal.TramoUtility;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.regsarima.regular.ILogLevelModule;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LogLevelModule implements ILogLevelModule {

    private static final String LL = "log-level test";
    private static final String NEG = "negative values, levels are chosen",
            FAILED = "log/level failed", LOGS = "logs are chosen", LEVELS = "levels are chosen";

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class Info {

        private final double logs;
        private final double levels;
        private final double logpreference;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("-LogLikelihood on logs=").append(new Formatter(Locale.ROOT).format("%6g", logs)).append("; ")
                    .append("-LogLikelihood on levels=").append(new Formatter(Locale.ROOT).format("%6g", levels)).append("; ")
                    .append("(log-preference)=").append(new Formatter(Locale.ROOT).format("%6g", logpreference)).append(')');
            return builder.toString();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(LogLevelModule.class)
    public static class Builder {

        private double precision = 1e-5;
        private double logpreference = 0;
        private boolean seasonal = true;

        public Builder logPreference(double lp) {
            this.logpreference = lp;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder seasonal(boolean seasonal) {
            this.seasonal = seasonal;
            return this;
        }

        public LogLevelModule build() {
            return new LogLevelModule(logpreference, precision, seasonal);
        }

    }

    private final double logpreference, precision;
    private final boolean seasonal;
    private double level, log = 1, slog;
    private RegArimaEstimation<SarimaModel> e, el;

    /**
     *
     */
    private LogLevelModule(double logpreference, double precision, boolean seasonal) {
        this.logpreference = logpreference;
        this.precision = precision;
        this.seasonal = seasonal;

    }

    /**
     *
     * @param ifreq
     * @param data
     * @param seas
     * @param model
     * @return
     */
    /**
     *
     */
    public void clear() {
        level = 0;
        log = 1;
        e = null;
        el = null;
    }

    /**
     *
     * @return
     */
    public RegArimaEstimation<SarimaModel> getLevelEstimation() {
        return e;
    }

    /**
     *
     * @return
     */
    public double getLevelLL() {
        return level;
    }

    /**
     *
     * @return
     */
    public RegArimaEstimation<SarimaModel> getLogEstimation() {
        return el;
    }

    /**
     *
     * @return
     */
    public double getLogLL() {
        return log;
    }

    /**
     *
     * @return
     */
    public double getLogPreference() {
        return logpreference;
    }

    /**
     *
     * @return
     */
    public boolean isChoosingLog() {
        return el == null ? false : log + logpreference < level;
    }

    /**
     *
     * @return @since 2.2
     */
    public double getLogCorrection() {
        return slog;
    }

    /**
     * @param data
     * @param frequency
     * @param variables
     * @param seas
     * @param log
     * @return
     */
    public boolean process(DoubleSeq data, int frequency, FastMatrix variables, boolean seas, ProcessingLog log) {
        RegArimaModel<SarimaModel> regarima = RegArimaUtility.airlineModel(data, true, frequency, seas);
        if (!variables.isEmpty()) {
            regarima = regarima.toBuilder().addX(variables).build();
        }
        return process(regarima, log);
    }

    public boolean process(RegArimaModel<SarimaModel> model, ProcessingLog logs) {
        IRegArimaComputer processor = TramoUtility.processor(true, precision);
        e = processor.process(model, null);
        if (e != null) {
            level = Math.log(e.getConcentratedLikelihood().ssq()
                    * e.getConcentratedLikelihood().factor());
        }

        double[] lx = model.getY().toArray();
        slog = 0;
        for (int i = 0; i < lx.length; ++i) {
            if (lx[i] <= 0) {
                return false;
            }
            lx[i] = Math.log(lx[i]);
            slog += lx[i];
        }
        slog /= lx.length;

        RegArimaModel<SarimaModel> logModel = model.toBuilder()
                .y(DoubleSeq.of(lx))
                .build();
        el = processor.process(logModel, null);

        if (el != null) {
            log = Math.log(el.getConcentratedLikelihood().ssq()
                    * el.getConcentratedLikelihood().factor())
                    + 2 * slog;
        }
        return true;
    }

    public TransformationType getTransformation() {
        return this.isChoosingLog() ? TransformationType.Log : TransformationType.None;
    }

    @Override
    public ProcessingResult process(RegSarimaModelling context) {
        ProcessingLog logs = context.getLog();
        logs.push(LL);
        try {
            ModelDescription desc = context.getDescription();
            DoubleSeq data = desc.getTransformedSeries().getValues();
            if (data.anyMatch(x -> x <= 0)) {
                logs.remark(NEG);
                return ProcessingResult.Unchanged;
            }
            FastMatrix variables = desc.regarima().variables();
            if (!process(data, desc.getAnnualFrequency(), variables, seasonal, logs)) {
                logs.warning(FAILED);
                return ProcessingResult.Failed;
            }
            Info info = new Info(log, level, logpreference);
            if (isChoosingLog()) {
                desc.setLogTransformation(true);
                context.clearEstimation();
                logs.info(LOGS, info);
                return ProcessingResult.Changed;
            } else {
                logs.info(LEVELS, info);
                return ProcessingResult.Unchanged;
            }
        } catch (RuntimeException err) {
            return ProcessingResult.Failed;
        } finally {
            logs.pop();
        }
    }

}
