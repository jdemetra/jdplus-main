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
package jdplus.sa.base.core.regarima;

import java.util.Formatter;
import java.util.Locale;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.AdditiveOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.IOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.LevelShiftFactory;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regsarima.regular.ILogLevelModule;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.regsarima.regular.RobustOutliersDetector;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;

/**
 * Identification of log/level transformation
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
            builder.append("AICC on logs=").append(new Formatter(Locale.ROOT).format("%6g", logs)).append("; ")
                    .append("AICC on levels=").append(new Formatter(Locale.ROOT).format("%6g", levels)).append("; ")
                    .append("(AICC-preference)=").append(new Formatter(Locale.ROOT).format("%6g", logpreference)).append(')');
            return builder.toString();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(LogLevelModule.class)
    public static class Builder {

        private double aiccdiff = -2;
        private double precision = 1e-7;
        private LengthOfPeriodType preadjust = LengthOfPeriodType.None;
        private boolean outliersCorrection = false;

        /**
         * When a pre-adjustment is specified, the model in logs is computed as
         * follows: - if the regression variables contain a length of
         * period/leap year, this variable (which must be named "lp") is removed
         * and the specified pre-adjustment is used instead. - otherwise, the
         * model is not modified (same model for logs and levels)
         *
         * The likelihood is automatically adjusted to take into account
         * possible transformations
         *
         * This feature is new in JD+ 3.0
         *
         * @param preadjust
         * @return
         * @since ("3.0")
         */
        public Builder preadjust(LengthOfPeriodType preadjust) {
            this.preadjust = preadjust;
            return this;
        }

        /**
         * Precision used in the estimation of the models (1e-5 by default). In
         * most cases, the precision can be smaller than for the estimation of
         * the final model.
         *
         * @param eps
         * @return
         */
        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        /**
         * Correction on the AICc of the model in logs. Negative corrections
         * will favour logs (-2 by default) Same as aiccdiff in the original
         * fortran program
         *
         * @param aiccdiff
         * @return
         */
        public Builder aiccLogCorrection(double aiccdiff) {
            this.aiccdiff = aiccdiff;
            return this;
        }

        public Builder outliersCorrection(boolean outliers) {
            this.outliersCorrection = outliers;
            return this;
        }

        public LogLevelModule build() {
            return new LogLevelModule(aiccdiff, precision, preadjust, outliersCorrection);
        }

    }

    private final double aiccDiff;
    private final double precision;
    private final LengthOfPeriodType preadjust;
    private final boolean outliersCorrection;
    private RegArimaEstimation<SarimaModel> level, log;
    private double aiccLevel, aiccLog;

    private LogLevelModule(double aiccdiff, final double precision, final LengthOfPeriodType preadjust, final boolean outliersCorrection) {
        this.aiccDiff = aiccdiff;
        this.precision = precision;
        this.preadjust = preadjust;
        this.outliersCorrection = outliersCorrection;
    }

    public double getEpsilon() {
        return precision;
    }

    /**
     *
     * @return
     */
    public boolean isChoosingLog() {
        if (log == null) {
            return false;
        } else if (level == null) {
            return true;
        } else {
            // the best is the smallest (default aiccdiff is negative to favor logs)
            return aiccLevel > aiccLog + aiccDiff;
        }
    }

    @Override
    public ProcessingResult process(RegSarimaModelling modelling) {
        clear();
        ProcessingLog logs = modelling.getLog();
        logs.push(LL);
        boolean toClean = false;
        try {
            ModelDescription model = modelling.getDescription();
            if (model.getSeries().getValues().anyMatch(z -> z <= 0)) {
                logs.remark(NEG);
                return ProcessingResult.Unchanged;
            }
            if (outliersCorrection) {
                toClean = outliers(modelling);
            }
            IRegArimaComputer processor = DemetraUtility.processor(true, precision);
            level = model.estimate(processor);

            ModelDescription logmodel = ModelDescription.copyOf(model);
            logmodel.setLogTransformation(true);
            if (preadjust != LengthOfPeriodType.None && logmodel.remove("lp")) {
                logmodel.setPreadjustment(preadjust);
            }
            log = logmodel.estimate(processor);
            if (level != null) {
                aiccLevel = level.statistics().getAICC();
            }
            if (log != null) {
                aiccLog = log.statistics().getAICC();
            }
            if (level == null && log == null) {
                logs.warning(FAILED);
                return ProcessingResult.Failed;
            }

            Info info = new Info(aiccLog, aiccLevel, aiccDiff);
            if (isChoosingLog()) {
                modelling.set(logmodel, log);
                logs.info(LOGS, info);
                return ProcessingResult.Changed;
            } else {
                logs.info(LEVELS, info);
                return ProcessingResult.Unchanged;
            }
        } finally {
            if (toClean) {
                modelling.getDescription().removeVariable(var -> ModellingUtility.isOutlier(var, true));
                modelling.clearEstimation();
            }
            logs.pop();
        }
    }

    private static final IOutlierFactory[] FAC = new IOutlierFactory[]{AdditiveOutlierFactory.FACTORY, LevelShiftFactory.FACTORY_ZEROENDED};

    private boolean outliers(RegSarimaModelling modelling) {
        RobustOutliersDetector detector = RobustOutliersDetector.builder()
                .criticalValue(5)
                .precision(1e-3)
                .build();
        ModelDescription desc = modelling.getDescription();
        TsData data = desc.getTransformedSeries();
        FastMatrix regs = desc.regarima().variables();
        try {
            detector.process(data.getValues(), data.getAnnualFrequency(), regs);
            int[][] outliers = detector.getOutliers();
            if (outliers.length == 0) {
                return false;
            } else {
                for (int i = 0; i < outliers.length; ++i) {
                    int[] cur = outliers[i];
                    TsPeriod pos = data.getPeriod(cur[0]);
                    IOutlier o = FAC[cur[1]].make(pos.start());
                    desc.addVariable(Variable.variable(IOutlier.defaultName(o.getCode(), pos), o, OutliersDetectionModule.attributes(o)));
                }
                return true;
            }
        } catch (Exception err) {
            return false;
        }
    }

    public TransformationType getTransformation() {
        return this.isChoosingLog() ? TransformationType.Log : TransformationType.None;
    }

    public double getAICcLevel() {
        return this.aiccLevel;
    }

    public double getAICcLog() {
        return this.aiccLog;
    }

    private void clear() {
        log = null;
        level = null;
        aiccLevel = 0;
        aiccLog = 0;
    }

    /**
     * @return the level_
     */
    public RegArimaEstimation<SarimaModel> getLevel() {
        return level;
    }

    /**
     * @return the log_
     */
    public RegArimaEstimation<SarimaModel> getLog() {
        return log;
    }

}
