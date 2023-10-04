/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.r.timeseries;

import com.google.protobuf.InvalidProtocolBufferException;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.timeseries.CalendarPeriodObs;
import jdplus.toolkit.base.api.timeseries.CalendarTimeSeries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtos;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtosUtility;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class TsUtility {

    public void updateProviders() {
        TsFactory nfactory = TsFactory.ofServiceLoader();
        TsFactory.setDefault(nfactory);
    }

    public TsData of(int freq, int year, int start, double[] data) {
        switch (freq) {
            case 1 -> {
                return TsData.ofInternal(TsPeriod.yearly(year), data);
            }
            case 12 -> {
                return TsData.ofInternal(TsPeriod.monthly(year, start), data);
            }
            default -> {
                int c = 12 / freq;
                TsPeriod pstart = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), LocalDate.of(year, (start - 1) * c + 1, 1));
                return TsData.ofInternal(pstart, data);
            }
        }
    }

    public TsDomain of(int freq, int year, int start, int len) {
        switch (freq) {
            case 1:
                return TsDomain.of(TsPeriod.yearly(year), len);
            case 12:
                return TsDomain.of(TsPeriod.monthly(year, start), len);
            default:
                int c = 12 / freq;
                TsPeriod pstart = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), LocalDate.of(year, (start - 1) * c + 1, 1));
                return TsDomain.of(pstart, len);
        }
    }

    public TsData aggregate(TsData source, int nfreq, String conversion, boolean fullperiods) {
        AggregationType agg = AggregationType.valueOf(conversion);
        if (agg == null) {
            return null;
        }
        TsUnit unit = TsUnit.ofAnnualFrequency(nfreq);
        return source.aggregate(unit, agg, fullperiods);
    }

    /**
     * Information useful for the conversion of series in R returns [freq, year,
     * period] (period is 1-based)
     *
     * @param s
     * @return
     */
    public int[] startPeriod(TsData s) {
        return of(s.getStart());
    }

    public int[] startPeriod(TsDataTable s) {
        return of(s.getDomain().getStartPeriod());
    }

    public int[] of(TsPeriod p) {
        LocalDate start = p.start().toLocalDate();
        int freq = p.getUnit().getAnnualFrequency();
        int c = 12 / freq;
        int mon = start.getMonthValue();
        int year = start.getYear();
        return new int[]{freq, year, 1 + (mon - 1) / c};
    }

    public CalendarTimeSeries of(String[] starts, double[] data) {
        if (starts.length != data.length + 1) {
            throw new IllegalArgumentException();
        }
        List<CalendarPeriodObs> entries = new ArrayList<>();
        LocalDate e = LocalDate.parse(starts[0], DateTimeFormatter.ISO_DATE), s = null;
        for (int i = 0; i < data.length; ++i) {
            s = e;
            entries.add(CalendarPeriodObs.of(s, e.plusDays(1), data[i]));
        }
        return CalendarTimeSeries.of(entries);
    }

    public CalendarTimeSeries of(String[] starts, String[] ends, double[] data) {
        if (starts.length != data.length || ends.length != data.length) {
            throw new IllegalArgumentException();
        }
        List<CalendarPeriodObs> entries = new ArrayList<>();

        for (int i = 0; i < data.length; ++i) {
            LocalDate s = LocalDate.parse(starts[i], DateTimeFormatter.ISO_DATE),
                    e = LocalDate.parse(ends[i], DateTimeFormatter.ISO_DATE);
            entries.add(CalendarPeriodObs.of(s, e.plusDays(1), data[i]));
        }
        return CalendarTimeSeries.of(entries);
    }

    public TsData cleanExtremities(TsData s) {
        return s.cleanExtremities();
    }
    
    public byte[] toBuffer(Ts s){
        return ToolkitProtosUtility.convert(s).toByteArray();
    }
    
    public Ts tsOfBytes(byte[] bytes) throws InvalidProtocolBufferException{
        return ToolkitProtosUtility.convert(ToolkitProtos.Ts.parseFrom(bytes));
    }
    
    public byte[] toBuffer(TsCollection s){
        return ToolkitProtosUtility.convert(s).toByteArray();
    }
    
    public TsCollection tsCollectionOfBytes(byte[] bytes) throws InvalidProtocolBufferException{
        return ToolkitProtosUtility.convert(ToolkitProtos.TsCollection.parseFrom(bytes));
    }
    
    private String dayOf(TsPeriod p, int pos){
        switch (pos){
            case 0 -> {
                return p.start().toLocalDate().format(DateTimeFormatter.ISO_DATE);
            }
            case 2 -> {
                return p.end().toLocalDate().minusDays(1).format(DateTimeFormatter.ISO_DATE);
            }
            default -> {
                LocalDate d0=p.start().toLocalDate(), d1=p.end().toLocalDate();
                return d0.plusDays(d0.until(d1, ChronoUnit.DAYS)/2).format(DateTimeFormatter.ISO_DATE);
            }
                
        }
    }
    
    /**
     * 
     * @param domain
     * @param pos 0 for first day, 1 for middle day, 2 for last day
     * @return 
     */  
    public String[] daysOf(TsDomain domain, int pos){
        String[] days=new String[domain.size()];
        for (int i=0; i<days.length; ++i){
            days[i]=dayOf(domain.get(i), pos);
        }
        return days;
    }
    
    public String[] daysOf(TsData data, int pos){
        return daysOf(data.getDomain(), pos);
    }
    
    public Ts makeTs(TsData data, String name){
        return Ts.of(name, data);
    }
    
    public Ts makeTs(TsMoniker moniker, String type){
        return TsFactory.getDefault().makeTs(moniker, TsInformationType.valueOf(type));
    }
    
    public TsCollection makeTsCollection(TsMoniker moniker, String type){
        return TsFactory.getDefault().makeTsCollection(moniker, TsInformationType.valueOf(type));
    }
    
}
