/*
 * Copyright 2019 National Bank of Belgium
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
package jdplus.x13.base.core.x13.regarima;

import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.x13.base.api.regarima.EasterSpec;
import jdplus.x13.base.api.regarima.EasterSpec.Type;
import jdplus.x13.base.api.regarima.MeanSpec;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.x13.base.api.regarima.RegressionSpec;
import jdplus.x13.base.api.regarima.RegressionTestSpec;
import jdplus.x13.base.api.regarima.TradingDaysSpec;
import jdplus.x13.base.api.regarima.TransformSpec;
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
import jdplus.toolkit.base.core.data.interpolation.AverageInterpolator;
import jdplus.toolkit.base.core.modelling.regression.AdditiveOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.HolidaysCorrectionFactory;
import jdplus.toolkit.base.core.modelling.regression.LevelShiftFactory;
import jdplus.toolkit.base.core.modelling.regression.PeriodicOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.TransitoryChangeFactory;
import jdplus.toolkit.base.core.regsarima.regular.IModelBuilder;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import nbbrd.design.Development;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class X13ModelBuilder implements IModelBuilder {

    public static final Map<String, String> calendarAMI;

    static {
        HashMap<String, String> map = new HashMap<>();
        map.put(ModellingUtility.AMI, "x13");
        map.put(SaVariable.REGEFFECT, ComponentType.CalendarEffect.name());
        calendarAMI = Collections.unmodifiableMap(map);
    }

    private final RegArimaSpec spec;
    private final ModellingContext context;

    public X13ModelBuilder(RegArimaSpec spec, ModellingContext context) {
        this.spec = spec;
        if (context != null) {
            this.context = context;
        } else {
            this.context = ModellingContext.getActiveContext();
        }
    }

    private void initializeArima(ModelDescription model) {
        int freq = model.getAnnualFrequency();
        boolean yearly = freq == 1;
        if (spec.isUsingAutoModel()) {
            model.setAirline(!yearly);
//            model.setMean(true);
        } else if (spec.getArima() == null) {
            model.setAirline(!yearly);
        } else {
            model.setArimaSpec(spec.getArima().withPeriod(freq));
        }
    }

    private void initializeVariables(ModelDescription model, RegressionSpec regSpec) {

        if (!regSpec.isUsed()) {
            return;
        }
        initializeMean(model, regSpec.getMean());
        initializeCalendar(model, regSpec, context);
        if (regSpec.getOutliersCount() > 0) {
            initializeOutliers(model, regSpec.getOutliers());
        }
        initializeUsers(model, regSpec.getUserDefinedVariables());
        initializeInterventions(model, regSpec.getInterventionVariables());
        initializeRamps(model, regSpec.getRamps());
    }

    @Override
    public ModelDescription build(TsData series, ProcessingLog log) {
        TsData nseries = TsDataToolkit.select(series, spec.getBasic().getSpan());
        TsDomain edom = nseries.getDomain().select(spec.getEstimate().getSpan());
        ModelDescription cur = new ModelDescription(nseries, edom);

        initializeMissing(cur);
        initializeTransformation(cur, spec.getTransform());
        initializeArima(cur);
        initializeVariables(cur, spec.getRegression());

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
        if (!mu.isUsed()) {
            model.setMean(false);
        } else if (Parameter.isFixed(mu.getCoefficient())) {
            int d = spec.getArima().getD(), bd = spec.getArima().getBd();
            add(model, new TrendConstant(d, bd), "const", ComponentType.Undefined, new Parameter[]{mu.getCoefficient()});
            model.setMean(false);
        } else {
            model.setMean(true);
        }
    }

    private void initializeCalendar(ModelDescription model, RegressionSpec calendar, ModellingContext context) {
        initializeTradingDays(model, calendar.getTradingDays(), context);
        initializeEaster(model, calendar.getEaster());
    }

    private void initializeTradingDays(ModelDescription model, TradingDaysSpec td, ModellingContext context) {
        if (!td.isUsed() || td.getRegressionTestType() == RegressionTestSpec.Add || td.isAutomatic()) {
            return;
        }
        if (td.isStockTradingDays()) {
            initializeStockTradingDays(model, td);
        } else if (td.getHolidays() != null) {
            initializeHolidays(model, td, context);
        } else if (td.getUserVariables() != null) {
            initializeUserTradingDays(model, td);
        } else {
            initializeDefaultTradingDays(model, td);
        }
    }

    private void initializeEaster(ModelDescription model, EasterSpec easter) {
        if (!easter.isUsed() || easter.getTest() == RegressionTestSpec.Add) {
            return;
        }
        if (easter.getTest() == RegressionTestSpec.None) {
            add(model, easter(easter.getType(), easter.getDuration()), "easter", ComponentType.CalendarEffect, easter.getCoefficient());
        } else {
            Variable<EasterVariable> var = Variable.variable("easter", easter(easter.getType(), easter.getDuration()), calendarAMI);
            model.addVariable(var);
        }

    }

    private void initializeOutliers(ModelDescription model, List<Variable<IOutlier>> outliers) {
        int freq = model.getAnnualFrequency();
        TransitoryChangeFactory tc = new TransitoryChangeFactory(TransitoryChangeFactory.rate(freq, spec.getOutliers().getMonthlyTCRate()));
        PeriodicOutlierFactory so = new PeriodicOutlierFactory(freq, true);
        for (Variable<IOutlier> outlier : outliers) {
            IOutlier cur = outlier.getCore();
            String code = cur.getCode();
            LocalDateTime pos = cur.getPosition();
            IOutlier v;
            ComponentType cmp = ComponentType.Undefined;
            switch (code) {
                case AdditiveOutlier.CODE:
                    v = AdditiveOutlierFactory.FACTORY.make(pos);
                    cmp = ComponentType.Irregular;
                    break;
                case LevelShift.CODE:
                    v = LevelShiftFactory.FACTORY_ZEROENDED.make(pos);
                    cmp = ComponentType.Trend;
                    break;
                case PeriodicOutlier.CODE:
                    v = so.make(pos);
                    cmp = ComponentType.Seasonal;
                    break;
                case TransitoryChange.CODE:
                    v = tc.make(pos);
                    cmp = ComponentType.Irregular;
                    break;
                default:
                    v = null;
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
            model.addVariable(user.withCore(user.getCore().instantiateFrom(context, user.getName())));
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

    private void initializeUserTradingDays(ModelDescription model, TradingDaysSpec td) {
        ITradingDaysVariable utd = userTradingDays(td, context);
        if (td.getRegressionTestType() == RegressionTestSpec.None) {
            add(model, utd, "usertd", ComponentType.CalendarEffect, td.getTdCoefficients());
        } else {
            Variable<ITradingDaysVariable> var = Variable.variable("usertd", utd, calendarAMI);
            model.addVariable(var);
        }
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td) {
        ITradingDaysVariable vtd = defaultTradingDays(td);
        ILengthOfPeriodVariable vlp = leapYear(td);
        if (td.getRegressionTestType() == RegressionTestSpec.None) {
            if (vtd != null) {
                add(model, vtd, "td", ComponentType.CalendarEffect, td.getTdCoefficients());
            }
            if (vlp != null) {
                add(model, vlp, "lp", ComponentType.CalendarEffect, td.getLpCoefficient());
            }
        } else {
            if (vtd != null) {
                Variable<ITradingDaysVariable> var = Variable.variable("td", vtd, calendarAMI);
                model.addVariable(var);
            }
            if (vlp != null) {
                Variable<ILengthOfPeriodVariable> lpvar = Variable.variable("lp", vlp, calendarAMI);
                model.addVariable(lpvar);
            }
        }
    }

    private void initializeHolidays(ModelDescription model, TradingDaysSpec td, ModellingContext context) {
        ITradingDaysVariable vtd = holidays(td, context);
        ILengthOfPeriodVariable vlp = leapYear(td);
        if (td.getRegressionTestType() == RegressionTestSpec.None) {
            if (vtd != null) {
                add(model, vtd, "td", ComponentType.CalendarEffect, td.getTdCoefficients());
            }
            if (vlp != null) {
                add(model, vlp, "lp", ComponentType.CalendarEffect, td.getLpCoefficient());
            }
        } else {
            if (vtd != null) {
                Variable<ITradingDaysVariable> var = Variable.variable("td", vtd, calendarAMI);
                model.addVariable(var);
            }
            if (vlp != null) {
                Variable<ILengthOfPeriodVariable> lpvar = Variable.variable("lp", vlp, calendarAMI);
                model.addVariable(lpvar);
            }
        }
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td) {
        if (td.getRegressionTestType() == RegressionTestSpec.None) {
            add(model, stockTradingDays(td), "td", ComponentType.CalendarEffect, td.getTdCoefficients());
        } else {
            Variable<ITradingDaysVariable> var = Variable.variable("td", stockTradingDays(td), calendarAMI);
            model.addVariable(var);
        }
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
    private void add(@NonNull ModelDescription model, @NonNull ITsVariable v, @NonNull String name, @NonNull ComponentType cmp, Parameter[] c) {
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
    private void add(@NonNull ModelDescription model, @NonNull ITsVariable v, @NonNull String name, @NonNull ComponentType cmp, Parameter c) {
        Variable var = Variable.builder()
                .name(name)
                .core(v)
                .coefficients(c == null ? null : new Parameter[]{c})
                .attribute(SaVariable.REGEFFECT, cmp.name())
                .build();
        model.addVariable(var);
    }

    public static ITradingDaysVariable tradingDays(RegArimaSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
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

    public static ITradingDaysVariable td(RegArimaSpec spec, DayClustering dc, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
        if (!tdspec.isUsed()) {
            return null;
        }
        if (tdspec.isStockTradingDays()) {
            return null;
        } else if (tdspec.getHolidays() != null) {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(tdspec.getHolidays(), context.getCalendars(), DayOfWeek.SUNDAY);
            return HolidaysCorrectedTradingDays.builder()
                    .clustering(dc)
                    .corrector(corrector)
                    .contrast(true)
                    .build();
        } else if (tdspec.getUserVariables() != null) {
            return null;
        } else {
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            return new GenericTradingDaysVariable(gtd);
        }
    }

    public static ITradingDaysVariable defaultTradingDays(TradingDaysSpec td) {
        if (td.getTradingDaysType() == TradingDaysType.NONE) {
            return null;
        }
        TradingDaysType tdType = td.getTradingDaysType();
        DayClustering dc = DayClustering.of(tdType);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return new GenericTradingDaysVariable(gtd);
    }

    public static ITradingDaysVariable holidays(TradingDaysSpec td, ModellingContext context) {
        if (td.getTradingDaysType() == TradingDaysType.NONE) {
            return null;
        }
        TradingDaysType tdType = td.getTradingDaysType();
        DayClustering dc = DayClustering.of(tdType);
        HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(td.getHolidays(), context.getCalendars(), DayOfWeek.SUNDAY);
        return HolidaysCorrectedTradingDays.builder()
                .clustering(dc)
                .corrector(corrector)
                .contrast(true)
                .build();
    }

    public static ITradingDaysVariable userTradingDays(TradingDaysSpec td, ModellingContext context) {
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

    public static IEasterVariable easter(Type type, int w) {
        switch (type) {
            case JulianEaster:
                return new JulianEasterVariable(w, true);
            case Easter:
                return EasterVariable.builder()
                        .duration(w)
                        .meanCorrection(EasterVariable.Correction.PreComputed)
                        .endPosition(-1)
                        .build();
            default:
                return null;
        }
    }

}
