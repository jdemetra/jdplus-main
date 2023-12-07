/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.base.core.regarima;

import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.modelling.regular.CalendarSpec;
import jdplus.toolkit.base.api.modelling.regular.EasterSpec;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.toolkit.base.api.modelling.regular.RegressionSpec;
import jdplus.toolkit.base.api.modelling.regular.TradingDaysSpec;
import jdplus.toolkit.base.api.modelling.regular.TransformSpec;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.api.timeseries.regression.EasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysCorrectedTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.IEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.ILengthOfPeriodVariable;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.JulianEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.LengthOfPeriod;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicOutlier;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.StockTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.TransitoryChange;
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.UserTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdplus.toolkit.base.api.modelling.regular.MeanSpec;
import jdplus.toolkit.base.core.data.interpolation.AverageInterpolator;
import jdplus.toolkit.base.core.modelling.regression.AdditiveOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.HolidaysCorrectionFactory;
import jdplus.toolkit.base.core.modelling.regression.LevelShiftFactory;
import jdplus.toolkit.base.core.modelling.regression.PeriodicOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.TransitoryChangeFactory;
import jdplus.toolkit.base.core.regsarima.regular.IModelBuilder;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import lombok.NonNull;

/**
 *
 * @author palatej
 */
public class ModelBuilder implements IModelBuilder{

    public static final Map<String, String> calendarAMI;

    static {
        HashMap<String, String> map = new HashMap<>();
        map.put(ModellingUtility.AMI, "demetra");
        map.put(SaVariable.REGEFFECT, ComponentType.CalendarEffect.name());
        calendarAMI = Collections.unmodifiableMap(map);
    }

    private final ModellingSpec spec;
    private final ModellingContext context;

    public ModelBuilder(ModellingSpec spec, ModellingContext context) {
        this.spec = spec;
        if (context != null) {
            this.context = context;
        } else {
            this.context = ModellingContext.getActiveContext();
        }
    }

    private void initializeArima(ModelDescription model) {
        int freq = model.getAnnualFrequency();
        model.setArimaSpec(spec.getArima().withPeriod(freq));
    }

    private void initializeVariables(ModelDescription model, RegressionSpec regSpec) {

        if (!regSpec.isUsed()) {
            return;
        }
        initializeMean(model, regSpec.getMean());
        initializeCalendar(model, regSpec.getCalendar());
        initializeOutliers(model, regSpec.getOutliers());
        initializeUsers(model, regSpec.getUserDefinedVariables());
        initializeInterventions(model, regSpec.getInterventionVariables());
        initializeRamps(model, regSpec.getRamps());
    }

    @Override
    public ModelDescription build(TsData series, ProcessingLog log) {
        TsData nseries = TsDataToolkit.select(series, spec.getSeries().getSpan());
        TsDomain edom = nseries.getDomain().select(spec.getEstimate().getSpan());
        ModelDescription cur = new ModelDescription(nseries, edom);

        initializeMissing(cur);
        initializeTransformation(cur, spec.getTransform());
        initializeVariables(cur, spec.getRegression());
        initializeArima(cur);// Mean is initialized here in case of auto-modelling (mean = true)

        return cur;
    }

    private void initializeMissing(ModelDescription cur) {
        cur.interpolate(AverageInterpolator.interpolator());
    }

    private void initializeTransformation(ModelDescription model, TransformSpec fnSpec) {
        if (fnSpec.getFunction() == TransformationType.Log) {
            model.setLogTransformation(true);
        }
        model.setPreadjustment(fnSpec.getAdjust());
    }

   private void initializeMean(ModelDescription model, MeanSpec mu) {
        if (! mu.isUsed()) {
            model.setMean(false);
        } else if (Parameter.isFixed(mu.getCoefficient())) {
            int d = spec.getArima().getD(), bd = spec.getArima().getBd();
            add(model, new TrendConstant(d, bd), "const", ComponentType.Undefined, new Parameter[]{mu.getCoefficient()});
            model.setMean(false);
        } else {
            model.setMean(true);
        }
    }

    private void initializeCalendar(ModelDescription model, CalendarSpec calendar) {
        initializeTradingDays(model, calendar.getTradingDays());
        initializeEaster(model, calendar.getEaster());
    }

    private void initializeTradingDays(ModelDescription model, TradingDaysSpec td) {
        if (!td.isUsed() || td.isTest() || td.isAutomatic()) {
            return;
        }
        if (td.isStockTradingDays()) {
            initializeStockTradingDays(model, td);
        } else if (td.getHolidays() != null) {
            initializeHolidays(model, td);
        } else if (td.getUserVariables() != null) {
            initializeUserTradingDays(model, td);
        } else {
            initializeDefaultTradingDays(model, td);
        }
    }

    private void initializeEaster(ModelDescription model, EasterSpec easter) {
        if (!easter.isUsed() || easter.isTest()) {
            return;
        }
        add(model, easter(spec), "easter", ComponentType.CalendarEffect, easter.getCoefficient());
    }

    private void initializeOutliers(ModelDescription model, List<Variable<IOutlier>> outliers) {
        int freq = model.getAnnualFrequency();
        TransitoryChangeFactory tc = new TransitoryChangeFactory(spec.getOutliers().getDeltaTC());
        PeriodicOutlierFactory so = new PeriodicOutlierFactory(freq, true);
        for (Variable<IOutlier> outlier : outliers) {
            IOutlier cur = outlier.getCore();
            String code = cur.getCode();
            LocalDateTime pos = cur.getPosition();
            IOutlier v;
            ComponentType cmp = ComponentType.Undefined;
            switch (code) {
                case AdditiveOutlier.CODE -> {
                    v = AdditiveOutlierFactory.FACTORY.make(pos);
                    cmp = ComponentType.Irregular;
                }
                case LevelShift.CODE -> {
                    v = LevelShiftFactory.FACTORY_ZEROENDED.make(pos);
                    cmp = ComponentType.Trend;
                }
                case PeriodicOutlier.CODE -> {
                    v = so.make(pos);
                    cmp = ComponentType.Seasonal;
                }
                case TransitoryChange.CODE -> {
                    v = tc.make(pos);
                    cmp = ComponentType.Irregular;
                }
                default -> v = null;
            }
            if (v != null) {
                Variable nvar = outlier.withCore(v);
                if (!nvar.hasAttribute(SaVariable.REGEFFECT)) {
                    nvar = nvar.setAttribute(SaVariable.REGEFFECT, cmp.name());
                }
                model.addVariable(nvar);
            }
        }
    }

    private void initializeUsers(ModelDescription model, List< Variable<TsContextVariable>> uvars) {
        for (Variable<TsContextVariable> user : uvars) {
            String name = user.getName();
            ITsVariable var = user.getCore().instantiateFrom(context, name);
            model.addVariable(user.withCore(var));
        }
    }

    private void initializeInterventions(ModelDescription model, List<Variable<InterventionVariable>> interventionVariables) {
        for (Variable<InterventionVariable> iv : interventionVariables) {
            model.addVariable(iv);
        }
    }

    private void initializeRamps(ModelDescription model, List<Variable<Ramp>> ramps) {
        for (Variable<Ramp> r : ramps) {
            model.addVariable(r);
        }
    }

    private void initializeHolidays(ModelDescription model, TradingDaysSpec td) {
        add(model, holidays(td, context), "td", ComponentType.CalendarEffect, td.getTdCoefficients());
        add(model, leapYear(td), "lp", ComponentType.CalendarEffect, td.getLpCoefficient());
    }

    private void initializeUserTradingDays(ModelDescription model, TradingDaysSpec td) {
        add(model, userTradingDays(td, context), "usertd", ComponentType.CalendarEffect, td.getTdCoefficients());
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td) {
        add(model, defaultTradingDays(td), "td", ComponentType.CalendarEffect, td.getTdCoefficients());
        add(model, leapYear(td), "lp", ComponentType.CalendarEffect, td.getLpCoefficient());
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td) {
        add(model, stockTradingDays(td), "td", ComponentType.CalendarEffect, td.getTdCoefficients());
    }

    private static ITradingDaysVariable stockTradingDays(TradingDaysSpec td) {
        return new StockTradingDays(td.getStockTradingDays());
    }

    /**
     * Add pre-specified variables (not automatically identified)
     *
     * @param model
     * @param v
     * @param name
     * @param cmp
     * @param c
     */
    private void add(@NonNull ModelDescription model, ITsVariable v, @NonNull String name, @NonNull ComponentType cmp, Parameter[] c) {
        if (v == null) {
            return;
        }
        Variable var = Variable.builder()
                .name(name)
                .core(v)
                .coefficients(c)
                .attribute(SaVariable.REGEFFECT, cmp.name())
                .build();
        model.addVariable(var);
    }

    /**
     * Add pre-specified variables (not automatically identified)
     *
     * @param model
     * @param v
     * @param name
     * @param cmp
     * @param c
     */
    private void add(@NonNull ModelDescription model, ITsVariable v, @NonNull String name, @NonNull ComponentType cmp, Parameter c) {
        if (v == null) {
            return;
        }
        Variable var = Variable.builder()
                .name(name)
                .core(v)
                .coefficients(c == null ? null : new Parameter[]{c})
                .attribute(SaVariable.REGEFFECT, cmp.name())
                .build();
        model.addVariable(var);
    }

    public static ITradingDaysVariable tradingDays(ModellingSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        if (!tdspec.isUsed()) {
            return null;
        }
        if (tdspec.isStockTradingDays()) {
            return new StockTradingDays(tdspec.getStockTradingDays());
        } else if (tdspec.getHolidays() != null) {
            return holidays(tdspec, context);
        } else if (tdspec.getUserVariables() != null) {
            return userTradingDays(tdspec, context);
        } else {
            return defaultTradingDays(tdspec);
        }
    }

    public static ITradingDaysVariable td(ModellingSpec spec, DayClustering dc, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        if (!tdspec.isUsed()) {
            return null;
        }
        if (tdspec.isStockTradingDays()) {
            return null;
        } else if (tdspec.getHolidays() != null) {
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(tdspec.getHolidays(), context.getCalendars(), DayOfWeek.SUNDAY);
            return HolidaysCorrectedTradingDays.builder().corrector(corrector).clustering(gtd.getClustering()).build();
        } else if (tdspec.getUserVariables() != null) {
            return null;
        } else {
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            return new GenericTradingDaysVariable(gtd);
        }
    }

    private static ITradingDaysVariable defaultTradingDays(TradingDaysSpec td) {
        if (td.getTradingDaysType() == TradingDaysType.NONE) {
            return null;
        }
        TradingDaysType tdType = td.getTradingDaysType();
        if (td.isAutomatic()) {
            tdType = TradingDaysType.TD7;
        }
        DayClustering dc = DayClustering.of(tdType);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return new GenericTradingDaysVariable(gtd);
    }

    private static ITradingDaysVariable holidays(TradingDaysSpec td, ModellingContext context) {
        if (td.getTradingDaysType() == TradingDaysType.NONE) {
            return null;
        }
        TradingDaysType tdType = td.getTradingDaysType();
        if (td.isAutomatic()) {
            tdType = TradingDaysType.TD7;
        }
        DayClustering dc = DayClustering.of(tdType);
        HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(td.getHolidays(), context.getCalendars(), DayOfWeek.SUNDAY);
        return HolidaysCorrectedTradingDays.builder()
                .clustering(dc)
                .corrector(corrector)
                .contrast(true)
                .build();
    }

    private static ITradingDaysVariable userTradingDays(TradingDaysSpec td, ModellingContext context) {
        String[] userVariables = td.getUserVariables();
        return UserTradingDays.of(userVariables, context);
    }

    public static ILengthOfPeriodVariable leapYear(TradingDaysSpec tdspec) {
        if (tdspec.getLengthOfPeriodType() == LengthOfPeriodType.None) {
            return null;
        } else {
            return new LengthOfPeriod(tdspec.getLengthOfPeriodType());
        }
    }

    public static IEasterVariable easter(EasterSpec.Type type, int w) {
        return switch (type) {
            case JULIANEASTER -> new JulianEasterVariable(w, true);
            case EASTER -> EasterVariable.builder()
                    .duration(w)
                    .meanCorrection(EasterVariable.Correction.Simple)
                    .endPosition(-1)
                    .build();
            default -> null;
        };
    }
    public static IEasterVariable easter(ModellingSpec spec) {
        EasterSpec espec = spec.getRegression().getCalendar().getEaster();
        if (!espec.isUsed()) {
            return null;
        }
        if (espec.isJulian()) {
            return new JulianEasterVariable(espec.getDuration(), true);
        } else {
            return EasterVariable.builder()
                    .duration(espec.getDuration())
                    .meanCorrection(EasterVariable.Correction.Simple)
                    .endPosition(-1)
                    .build();
        }
    }

}
