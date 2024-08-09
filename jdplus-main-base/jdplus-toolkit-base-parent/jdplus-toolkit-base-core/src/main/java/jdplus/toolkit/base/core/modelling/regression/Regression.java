/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsException;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.api.timeseries.regression.Constant;
import jdplus.toolkit.base.api.timeseries.regression.EasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysCorrectedTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.JulianEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.LengthOfPeriod;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import jdplus.toolkit.base.api.timeseries.regression.LinearTrend;
import jdplus.toolkit.base.api.timeseries.regression.ModifiedTsVariable;
import jdplus.toolkit.base.api.timeseries.regression.MovingHolidayVariable;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicContrasts;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicDummies;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicOutlier;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.StockTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.SwitchOutlier;
import jdplus.toolkit.base.api.timeseries.regression.TransitoryChange;
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.api.timeseries.regression.TrigonometricVariables;
import jdplus.toolkit.base.api.timeseries.regression.TsVariable;
import jdplus.toolkit.base.api.timeseries.regression.TsVariables;
import jdplus.toolkit.base.api.timeseries.regression.UserMovingHoliday;
import jdplus.toolkit.base.api.timeseries.regression.UserTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.UserVariable;
import jdplus.toolkit.base.api.timeseries.regression.UserVariables;
import java.util.HashMap;
import java.util.Map;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixWindow;
import lombok.NonNull;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Regression {

    private final Map< Class<? extends ITsVariable>, RegressionVariableFactory> FACTORIES
            = new HashMap<>();

    public <V extends ITsVariable, W extends V> boolean register(Class<W> wclass, RegressionVariableFactory<V> factory) {
        synchronized (FACTORIES) {
            if (FACTORIES.containsKey(wclass)) {
                return false;
            }
            FACTORIES.put(wclass, factory);
            return true;
        }
    }

    public <V extends ITsVariable> boolean unregister(Class<V> vclass) {
        synchronized (FACTORIES) {
            RegressionVariableFactory removed = FACTORIES.remove(vclass);
            return removed != null;
        }
    }

    static {
        synchronized (FACTORIES) {
            // Basic
            FACTORIES.put(Constant.class, ConstantFactory.FACTORY);
            FACTORIES.put(LinearTrend.class, LinearTrendFactory.FACTORY);
            FACTORIES.put(TrendConstant.class, TrendConstantFactory.FACTORY);

            // Outliers
            FACTORIES.put(AdditiveOutlier.class, AOFactory.FACTORY);
            FACTORIES.put(LevelShift.class, LSFactory.FACTORY);
            FACTORIES.put(TransitoryChange.class, TCFactory.FACTORY);
            FACTORIES.put(SwitchOutlier.class, WOFactory.FACTORY);
            FACTORIES.put(PeriodicOutlier.class, SOFactory.FACTORY);

            // Trading Days
            FACTORIES.put(LengthOfPeriod.class, LPFactory.FACTORY);
            FACTORIES.put(GenericTradingDaysVariable.class, GenericTradingDaysFactory.FACTORY);
            FACTORIES.put(HolidaysCorrectedTradingDays.class, HolidaysCorrectionFactory.FACTORY);
            FACTORIES.put(StockTradingDays.class, StockTDFactory.FACTORY);
            FACTORIES.put(HolidaysVariable.class, HolidaysFactory.FACTORY);

            // Moving holidays
            FACTORIES.put(EasterVariable.class, EasterFactory.FACTORY);
            FACTORIES.put(JulianEasterVariable.class, JulianEasterFactory.FACTORY);
            FACTORIES.put(MovingHolidayVariable.class, MovingHolidayFactory.FACTORY);

            // Others
            FACTORIES.put(Ramp.class, RampFactory.FACTORY);
            FACTORIES.put(InterventionVariable.class, IVFactory.FACTORY);
            FACTORIES.put(PeriodicDummies.class, PeriodicDummiesFactory.FACTORY);
            FACTORIES.put(PeriodicContrasts.class, PeriodicContrastsFactory.FACTORY);
            FACTORIES.put(TrigonometricVariables.class, TrigonometricVariablesFactory.FACTORY);

            FACTORIES.put(TsVariable.class, TsVariableFactory.FACTORY);
            FACTORIES.put(UserVariable.class, TsVariableFactory.FACTORY);
            FACTORIES.put(UserMovingHoliday.class, TsVariableFactory.FACTORY);
            FACTORIES.put(TsVariables.class, TsVariablesFactory.FACTORY);
            FACTORIES.put(UserVariables.class, TsVariablesFactory.FACTORY);
            FACTORIES.put(UserTradingDays.class, TsVariablesFactory.FACTORY);
            
            FACTORIES.put(ModifiedTsVariable.class, ModifiedTsVariableFactory.FACTORY);
        }
    }
    
    static RegressionVariableFactory factoryFor(ITsVariable var){
       synchronized (FACTORIES) {
             return FACTORIES.get(var.getClass());
        }
        
    }
   

    public <D extends TimeSeriesDomain> FastMatrix matrix(@NonNull D domain, @NonNull ITsVariable... vars) {
        if (domain.isEmpty() || vars.length == 0) {
            return FastMatrix.EMPTY;
        }
        int nvars = ITsVariable.dim(vars);
        int nobs = domain.length();
        FastMatrix M = FastMatrix.make(nobs, nvars);

        MatrixWindow wnd = M.left(0);
        if (domain instanceof TsDomain tsDomain) {
            TsPeriod start = tsDomain.getStartPeriod();
            for (int i = 0, j = 0; i < vars.length; ++i) {
                ITsVariable v = vars[i];
                RegressionVariableFactory factory = FACTORIES.get(v.getClass());
                if (factory == null) {
                    throw new TsException("Unknown variable");
                }
                factory.fill(v, start, wnd.hnext(v.dim()));
            }
        } else {
            for (int i = 0, j = 0; i < vars.length; ++i) {
                ITsVariable v = vars[i];
                RegressionVariableFactory factory = FACTORIES.get(v.getClass());
                if (factory == null) {
                    throw new TsException("Unknown variable");
                }
                factory.fill(v, domain, wnd.hnext(v.dim()));
            }
        }
        return M;
    }

    public <D extends TimeSeriesDomain> DataBlock x(@NonNull D domain, @NonNull ITsVariable vars) {
        if (vars.dim() != 1) {
            throw new IllegalArgumentException();
        }
        FastMatrix m = matrix(domain, vars);
        return DataBlock.of(m.getStorage());
    }

}
