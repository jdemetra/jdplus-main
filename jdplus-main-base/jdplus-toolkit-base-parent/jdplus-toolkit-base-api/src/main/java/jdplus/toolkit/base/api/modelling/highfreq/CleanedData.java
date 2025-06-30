/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.toolkit.base.api.modelling.highfreq;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import java.util.Arrays;

/**
 *
 * @author palatej
 */

public final class CleanedData {
    
    public static CleanedData of(TsData data){
        return new CleanedData(data, DataCleaning.of(data));
    }

    public static CleanedData of(TsData data, DataCleaning cleaning){
        return new CleanedData(data, cleaning == null ? DataCleaning.of(data) : cleaning);
    }

    private CleanedData(TsData data, DataCleaning cleaning){
        this.cleaning=cleaning;
        this.domain=data.getDomain();
        clean(cleaning, data);
    }


    @lombok.Getter
    private DataCleaning cleaning; 
    /**
     * Cleaned data;
     */
    private DoubleSeq data;
    /**
     * Positions of the data relative to the original domain. Null if no cleaning is applied
     */
    private int[] positions;
    /**
     * Reference domain (domain of the original data)
     */
    @lombok.Getter
    private TsDomain domain;
    
    public DoubleSeq getData(){
        return data;
    }
    
    private int tpos(int idx){
        return positions == null ? idx : positions[idx];
    }
    
    public TsPeriod getPeriod(int idx){
        return domain.get(tpos(idx));
    }
    
    public double getValue(int idx){
        return data.get(idx);
    }
    
    public int size(){
        return data.length();
    }
    
    private void clean(DataCleaning cleaning, TsData s) {
        switch (cleaning) {
            case SUNDAYS ->
                cleanSundays(s);
            case WEEKENDS ->
                cleanWeekEnds(s);
            default ->{
                set(s);
            }
        }
    }
    
    private void set(TsData s){
        data=s.getValues();
    }

    private void cleanSundays(TsData s) {
        double[] tmp = new double[s.length()];
        int[] tpos = new int[tmp.length];
        TsPeriod start = s.getStart();
        if (!start.getUnit().equals(TsUnit.P1D)) {
            throw new java.lang.UnsupportedOperationException();
        }
        int pos = start.start().getDayOfWeek().getValue();
        DoubleSeqCursor cursor = s.getValues().cursor();
        int cur = 0;
        for (int i = 0; i < tmp.length; ++i) {
            if (pos != 7) {
                tpos[cur]=i;
                tmp[cur++] = cursor.getAndNext();
                ++pos;
            } else {
                cursor.skip(1);
                pos = 1;
            }
        }
        data=DoubleSeq.of(tmp, 0, cur);
        positions=Arrays.copyOf(tpos, cur);
    }

    private void cleanWeekEnds(TsData s) {
        double[] tmp = new double[s.length()];
        int[] tpos = new int[tmp.length];
        TsPeriod start = s.getStart();
        if (!start.getUnit().equals(TsUnit.P1D)) {
            throw new java.lang.UnsupportedOperationException();
        }
        int pos = start.start().getDayOfWeek().getValue(), i = 0;
        DoubleSeqCursor cursor = s.getValues().cursor();
        if (pos == 7) {
            cursor.skip(1);
            pos = 1;
            i = 1;
        }
        int cur = 0;
        while (i < tmp.length) {
            if (pos != 6) {
                tpos[cur]=i;
                tmp[cur++] = cursor.getAndNext();
                ++pos;
                ++i;
            } else {
                cursor.skip(2);
                pos = 1;
                i += 2;
            }
        }
        data=DoubleSeq.of(tmp, 0, cur);
        positions=Arrays.copyOf(tpos, cur);
    }


}
