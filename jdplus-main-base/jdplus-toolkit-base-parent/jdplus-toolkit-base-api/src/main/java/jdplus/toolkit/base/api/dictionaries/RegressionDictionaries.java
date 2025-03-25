/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.api.dictionaries;

import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegressionDictionaries {

    public static final String LOG = "log", ADJUST = "adjust", PERIOD = "period",
            SPAN_START = "span.start", SPAN_END = "span.end", SPAN_N = "span.n", SPAN_MISSING = "span.missing";

    public final Dictionary BASIC = AtomicDictionary.builder()
            .name("basic")
            .item(AtomicDictionary.Item.builder().name(PERIOD).description("period of the series").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(SPAN_START).description("start of the considered (partial) series").outputClass(TsPeriod.class).build())
            .item(AtomicDictionary.Item.builder().name(SPAN_END).description("end of the considered (partial) series").outputClass(TsPeriod.class).build())
            .item(AtomicDictionary.Item.builder().name(SPAN_N).description("number of periods in the considered (partial) series").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(SPAN_MISSING).description("number of missing values in the considered (partial) series").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(LOG).description("log-transformtion").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(ADJUST).description("pre-adjustment for leap year").outputClass(String.class).build())
            .build();

    public static final String ESPAN_START = "espan.start", ESPAN_END = "espan.end", ESPAN_N = "espan.n", ESPAN_MISSING = "espan.missing",
            MEAN = "mean", NLP = "nlp", NTD = "ntd", LEASTER = "leaster", NMH = "nmh", NOUT = "nout", NAO = "nao", NLS = "nls", NTC = "ntc", NSO = "nso", NUSERS = "nusers",
            MU = "mu", LP = "lp", TD = "td", TDDERIVED = "td-derived", TDF = "td-ftest", EASTER = "easter", OUTLIERS = "outlier", USER = "user", MISSING = "missing";

    public final Dictionary REGRESSION_DESC = AtomicDictionary.builder()
            .name("regression")
            .item(AtomicDictionary.Item.builder().name(ESPAN_START).description("start of the considered span in the estimation").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(ESPAN_END).description("end of the considered span in the estimation").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(ESPAN_N).description("number of periods in the considered span for estimation").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(ESPAN_MISSING).description("number of missing values in the considered span for estimation").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(MEAN).description("is trend constant").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NLP).description("is leap year").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NTD).description("number of trading days (outside lp)").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(LEASTER).description("length of the easter effect").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NMH).description("number of moving holidays").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NOUT).description("number of outliers").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NAO).description("number of additive outliers").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NLS).description("number of level shifts").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NTC).description("number of transitory changes").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NSO).description("number of seasonal outliers").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NUSERS).description("number of user-defined variables").outputClass(Integer.class).build())
            .build();

    public final Dictionary REGRESSION_EST = AtomicDictionary.builder()
            .name("regression estimation")
            .item(AtomicDictionary.Item.builder().name(MU).description("trend constant").outputClass(RegressionItem.class).build())
            .item(AtomicDictionary.Item.builder().name(LP).description("leap year effect").outputClass(RegressionItem.class).build())
            .item(AtomicDictionary.Item.builder().name(TD).description("trading days effect").outputClass(RegressionItem.class).type(Dictionary.EntryType.Array).build())
            .item(AtomicDictionary.Item.builder().name(TDDERIVED).description("derived trading day effect (contrast)").outputClass(RegressionItem.class).build())
            .item(AtomicDictionary.Item.builder().name(TDF).description("derived trading day effect (contrast)").outputClass(StatisticalTest.class).build())
            .item(AtomicDictionary.Item.builder().name(EASTER).description("easter effect").outputClass(RegressionItem.class).build())
            .item(AtomicDictionary.Item.builder().name(OUTLIERS).description("outliers").outputClass(RegressionItem.class).type(Dictionary.EntryType.Array).build())
            .item(AtomicDictionary.Item.builder().name(USER).description("user variables").outputClass(RegressionItem.class).type(Dictionary.EntryType.Array).build())
            .item(AtomicDictionary.Item.builder().name(MISSING).description("estimation of missing values").outputClass(MissingValueEstimation.class).type(Dictionary.EntryType.Array).build())
            .build();

    public static final String Y = "y", // original series
            YC = "yc", // interpolated series. Untransformed
            Y_F = "y_f",
            Y_EF = "y_ef",
            Y_B = "y_b",
            Y_EB = "y_eb",
            YC_F = "yc_f",
            YC_B = "yc_b",
            YLIN = "ylin", // linearized series (transformed series without pre-adjustment and regression effects). Untransformed
            YLIN_F = "ylin_f",
            YLIN_B = "ylin_b",
            CAL = "cal", // all calendar effects (including pre-adjustments). cal=tde+*mhe. Untransformed       
            CAL_F = "cal_f",
            CAL_B = "cal_b",
            YCAL = "ycal", // series corrected for calendar effects: y_cal = yc-/cal. Untransformed
            YCAL_F = "ycal_f",
            YCAL_B = "ycal_b",
            DET = "det", // all deterministic effects (including pre-adjustment). Untransformed
            DET_F = "det_f",
            DET_B = "det_b",
            TDE = "tde", // trading days effects (including leap year/length of period, includeing pre-adjustments). Untransformed
            TDE_F = "tde_f",
            TDE_B = "tde_b",
            EE = "ee", // Easter effects. Untransformed
            EE_F = "ee_f",
            EE_B = "ee_b",
            RMDE = "rmde", // Ramadan effects. Untransformed
            RMDE_F = "rmde_f",
            RMDE_B = "rmde_b",
            OMHE = "omhe", // Other mothing holidays effects. Untransformed
            OMHE_F = "omhe_f",
            OMHE_B = "omhe_b",
            MHE = "mhe", // All moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed
            MHE_F = "mhe_f",
            MHE_B = "mhe_b",
            OUT = "out", // All outliers effects. Untransformed
            OUT_F = "out_f",
            OUT_B = "out_b",
            REG = "reg", // All other regression effects (outside outliers and calendars). Untransformed
            REG_F = "reg_f",
            REG_B = "reg_b",
            L = "l", // linearized series (series without pre-adjustment and regression effects). l=yc-/det. Transformed
            L_F = "l_f",
            L_EF = "l_ef",
            L_B = "l_b",
            L_EB = "l_eb",
            FULL_RES="full_res" // full residuals. L^-1*([log]yc-[log]det)
            ;

    public final Dictionary REGRESSION_EFFECTS = AtomicDictionary.builder()
            .name("regression estimation")
            .item(AtomicDictionary.Item.builder().name(Y).description("original series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(Y_F).description("forecasts of the original series").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(Y_EF).description("forecasts errors of the original series").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(Y_B).description("backcasts of the original series").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(Y_EB).description("backcasts errors of the original series").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(YC).description("interpolated series. Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(YC_F).description("forecasts of the interpolated series").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(YC_B).description("backcasts of the interpolated series").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(YLIN).description("linearized series (series without pre-adjustment and regression effects). l=yc-/det. Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(YLIN_F).description("forcasts of the linearized series. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(YLIN_B).description("backcasts of the linearized series. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(DET).description("all deterministic effects (including pre-adjustment). Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(DET_F).description("forcasts of all deterministic effects (including pre-adjustment). Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(DET_B).description("backcasts of all deterministic effects (including pre-adjustment). Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(CAL).description("all calendar effects (including pre-adjustments). cal=tde+*mhe. Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(CAL_F).description("forecasts of all calendar effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(CAL_B).description("backcasts of all calendar effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(YCAL).description("series corrected for calendar effects: y_cal = yc-/cal. Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(YCAL_F).description("forecasts of the series corrected for calendar effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(YCAL_B).description("backcasts of the series corrected for calendar effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(TDE).description("trading days effects (including leap year/length of period, including pre-adjustments). Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(TDE_F).description("forecasts of the trading days effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(TDE_B).description("backcasts of the trading days effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(EE).description("Easter effects. Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(EE_F).description("forecasts of the Easter effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(EE_B).description("backcasts of the Easter effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(OMHE).description("other mothing holidays effects. Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(OMHE_F).description("forecasts of the other mothing holidays effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(OMHE_B).description("backcasts of the other mothing holidays effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(MHE).description("all moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(MHE_F).description("forecats of all moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(MHE_B).description("backcasts of all moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(OUT).description("all outliers effects. Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(OUT_F).description("forecasts of all outliers effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(OUT_B).description("backcasts of all outliers effects. Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(REG).description("all other regression effects (outside outliers and calendars). Untransformed").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(REG_F).description("forecasts of all other regression effects (outside outliers and calendars). Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(REG_B).description("backcasts of all other regression effects (outside outliers and calendars). Untransformed").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(L).description("linearized series (transformed series without pre-adjustment and regression effects). Transformed)").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(L_F).description("forecasts of the linearized series. Transformed)").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(L_B).description("backcasts of the linearized series. Transformed)").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
//            .item(AtomicDictionary.Item.builder().name(L_EF).description("forecast errors of the linearized series. Transformed)").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
//            .item(AtomicDictionary.Item.builder().name(L_EB).description("backcast errors of the linearized series. Transformed)").outputClass(TsData.class).type(Dictionary.EntryType.Parametric).build())
            .item(AtomicDictionary.Item.builder().name(FULL_RES).description("full residuals").outputClass(TsData.class).build())
            .build();

    public final String COEFFDESC = "description", REGTYPE = "type", COEFF = "coefficients", COVAR = "covar", COVAR_ML = "covar-ml";
    
    public final Dictionary REGRESSION_UTILITY = AtomicDictionary.builder()
            .name("regression utility")
            .item(AtomicDictionary.Item.builder().name(COEFFDESC).description("description of all the regression variables").outputClass(String[].class).build())
            .item(AtomicDictionary.Item.builder().name(REGTYPE).description("type of all the regression variables").outputClass(int[].class).build())
            .item(AtomicDictionary.Item.builder().name(COEFF).description("coeff of all the regression variables").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(COVAR).description("covariance of the parameters of all the regression variables").outputClass(Matrix.class).build())
            .item(AtomicDictionary.Item.builder().name(COVAR_ML).description("ml-covariance of the parameters of all the regression variables").outputClass(Matrix.class).build())
            .build();
}
