/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.information;

import jdplus.toolkit.base.api.information.InformationException;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicOutlier;
import jdplus.toolkit.base.api.timeseries.regression.TransitoryChange;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class OutlierMapping {

    public final String TYPE = "type", POS = "pos", PERIOD = "period", TCRATE = "rate", ZEROENDED = "zeroended";

    public InformationSet write(IOutlier o, boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(TYPE, o.getCode());
        info.set(POS, o.getPosition().toLocalDate().format(DateTimeFormatter.ISO_DATE));

        if (o instanceof LevelShift) {
            LevelShift ls = (LevelShift) o;
            if (verbose || !ls.isZeroEnded()) {
                info.set(ZEROENDED, ls.isZeroEnded());
            }
        } else if (o instanceof TransitoryChange) {
            TransitoryChange tc = (TransitoryChange) o;
            info.set(TCRATE, tc.getRate());
        } else if (o instanceof PeriodicOutlier) {
            PeriodicOutlier so = (PeriodicOutlier) o;
            if (verbose || !so.isZeroEnded()) {
                info.set(ZEROENDED, so.isZeroEnded());
            }
            if (verbose || so.getPeriod() != 0) {
                info.set(PERIOD, so.getPeriod());
            }
        }

        return info;
    }

    public IOutlier read(InformationSet info) {
        String st = info.get(TYPE, String.class);
        String sp = info.get(POS, String.class);
        LocalDateTime pos = LocalDate.parse(sp, DateTimeFormatter.ISO_DATE).atStartOfDay();
        switch (st) {
            case AdditiveOutlier.CODE:
                return new AdditiveOutlier(pos);
            case LevelShift.CODE: {
                Boolean zeroended = info.get(ZEROENDED, Boolean.class);
                return new LevelShift(pos, zeroended == null ? true : zeroended);
            }
            case TransitoryChange.CODE: {
                Double tc = info.get(TCRATE, Double.class);
                return new TransitoryChange(pos, tc);
            }
            case PeriodicOutlier.CODE:
            case PeriodicOutlier.PO: {
                Boolean zeroended = info.get(ZEROENDED, Boolean.class);
                Integer period = info.get(PERIOD, Integer.class);
                return new PeriodicOutlier(pos, period == null ? 0 : period, zeroended == null ? true : zeroended);
            }
            default:
                throw new InformationException("not supported outlier");
        }
    }

    public IOutlier from(OutlierDefinition def) {
        switch (def.getCode()) {
            case "AO":
            case "ao":
                return new AdditiveOutlier(def.getPosition().atStartOfDay());
            case "LS":
            case "ls":
                return new LevelShift(def.getPosition().atStartOfDay(), true);
            case "TC":
            case "tc":
                return new TransitoryChange(def.getPosition().atStartOfDay(), .7);
            case "SO":
            case "s0":
                return new PeriodicOutlier(def.getPosition().atStartOfDay(), 0, true);
            default:
                return null;
        }
    }

    public String format(IOutlier o) {
        StringBuilder builder = new StringBuilder();
        builder.append(InformationSet.concatenate(o.getCode(), o.getPosition().toLocalDate().format(DateTimeFormatter.ISO_DATE)));
        return builder.toString();
    }
    
    public String name(IOutlier o, TsDomain context){
        StringBuilder builder = new StringBuilder();
        builder.append(o.getCode()).append(" (");
        int period = context == null ? 0 : context.getAnnualFrequency();
        if (period <= 0)
            builder.append(o.getPosition().toLocalDate());
        else{
            TsPeriod p= TsPeriod.of(TsUnit.ofAnnualFrequency(period), o.getPosition());
            builder.append(p.getStartAsShortString());
        }
        builder.append(')');
        return builder.toString();
        
    }
    
}
