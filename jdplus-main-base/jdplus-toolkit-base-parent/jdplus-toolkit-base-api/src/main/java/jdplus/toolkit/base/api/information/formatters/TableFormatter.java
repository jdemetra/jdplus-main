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
package jdplus.toolkit.base.api.information.formatters;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.math.Complex;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;
import jdplus.toolkit.base.api.util.Table;
import nbbrd.design.SystemDependent;

import java.util.*;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TableFormatter {

    private static final Map<Class<?>, InformationFormatter> DICTIONARY = initDictionary();
    private static final Locale LOCALE = initLocale();

    private static Map<Class<?>, InformationFormatter> initDictionary() {
        Map<Class<?>, InformationFormatter> result = new HashMap<>();
        result.put(double.class, new DoubleFormatter());
        result.put(int.class, new IntegerFormatter());
        result.put(long.class, new LongFormatter());
        result.put(boolean.class, new BooleanFormatter("1", "0"));
        result.put(Double.class, new DoubleFormatter());
        result.put(Integer.class, new IntegerFormatter());
        result.put(Long.class, new LongFormatter());
        result.put(Boolean.class, new BooleanFormatter("1", "0"));
        result.put(Complex.class, new ComplexFormatter());
        result.put(String.class, new StringFormatter());
        result.put(String[].class, new StringArrayFormatter());
        result.put(SarimaOrders.class, new SarimaFormatter());
        result.put(Parameter.class, new ParameterFormatter());
//        result.put(ParameterInfo.class, new ParameterInfoFormatter());
//        result.put(TsMoniker.class, new MonikerFormatter());
        result.put(TsPeriod.class, new PeriodFormatter());
        result.put(RegressionItem.class, new RegressionItemFormatter());
        result.put(StatisticalTest.class, new StatisticalTestFormatter());
        result.put(ProcDiagnostic.class, new DiagnosticFormatter());
        return result;
    }

    @SystemDependent
    private static Locale initLocale() {
        return Locale.getDefault();
    }

    public Table<String> formatInformation(List<InformationSet> records, List<String> names, boolean shortname) {

        List<String[]> snames = new ArrayList<>();
        for (String name : names) {
            snames.add(InformationSet.split(name));
        }
        int[] witems = new int[snames.size()];
        int ncols = 0;
        int icur = 0;
        for (String[] sname : snames) {

            String slast = sname[sname.length - 1];
            int l = slast.indexOf(':');
            int w = 1;
            if (l >= 0) {

                String s0 = slast.substring(0, l);
                String s1 = slast.substring(l + 1);
                try {
                    w = Integer.parseInt(s1);
                    sname[sname.length - 1] = s0;
                } catch (Exception ex) {
                }
            }
            witems[icur] = w;
            if (w < 0) {
                ++ncols;
            } else {
                ncols += w;
            }
            icur++;
        }
        Table<String> rslt = new Table<>(records.size(), ncols);

        int row = 0;
        for (InformationSet record : records) {
            if (record == null) {
                continue;
            }
            int c = 0, col = 0;
            for (String[] cnames : snames) {
                int n = witems[c++];
                Object obj = record.search(cnames, Object.class);
                if (obj != null) {
                    if (n == 1) {
                        rslt.set(row, col++, format(obj, 0));
                    } else if (n < 0) {
                        rslt.set(row, col++, format(obj, n));
                    } else {
                        for (int j = 1; j <= n; ++j) {
                            rslt.set(row, col++, format(obj, j));
                        }
                    }
                } else if (n < 0) {
                    ++col;
                } else {
                    col += n;
                }

            }
            ++row;
        }
        return rslt;
    }

    public Table<String> formatProcResults(List<Explorable> records, List<String> names, boolean shortname) {

        // TODO. inefficient. Should be improved
        List<String[]> snames = new ArrayList<>();
        for (String name : names) {
            snames.add(InformationSet.split(name));
        }
        int[] witems = new int[snames.size()];
        int ncols = 0;
        int icur = 0;
        for (String[] sname : snames) {

            String slast = sname[sname.length - 1];
            int l = slast.indexOf(':');
            int w = 1;
            if (l >= 0) {

                String s0 = slast.substring(0, l);
                String s1 = slast.substring(l + 1);
                try {
                    w = Integer.parseInt(s1);
                    sname[sname.length - 1] = s0;
                } catch (Exception ex) {
                }
            }
            witems[icur] = w;
            if (w < 0) {
                ++ncols;
            } else {
                ncols += w;
            }
            icur++;
        }
        Table<String> rslt = new Table<>(records.size(), ncols);

        int row = 0;
        for (Explorable record : records) {
            if (record == null) {
                continue;
            }
            int c = 0, col = 0;
            for (String[] cnames : snames) {
                int n = witems[c++];
                Object obj = record.getData(InformationSet.concatenate(cnames), Object.class);
                if (obj != null) {
                    if (n == 1) {
                        rslt.set(row, col++, format(obj, 0));
                    } else if (n < 0) {
                        rslt.set(row, col++, format(obj, n));
                    } else if (n > 1) {
                        for (int j = 1; j <= n; ++j) {
                            rslt.set(row, col++, format(obj, j));
                        }
                    }
                } else if (n < 0) {
                    ++col;
                } else {
                    col += n;
                }
            }
            ++row;
        }
        return rslt;
    }

    private String format(Object obj, int item) {
        InformationFormatter fmt;
        try {
            if (obj != null && obj.getClass().isArray()) {
                Object[] array = (Object[]) obj;
                fmt = DICTIONARY.get(array[0].getClass());
                Object[] rslt = Arrays.stream(array).map(i -> fmt.format(i, item, LOCALE)).toArray();
                return Arrays.toString(rslt);
            } else {
                fmt = DICTIONARY.get(obj.getClass());
            }

            return fmt.format(obj, item, LOCALE);
        } catch (Exception ex) {
            String msg = ex.getMessage();
        }

        if (item == 0) {
            return obj.toString();
        } else {
            return "";
        }
    }
}
