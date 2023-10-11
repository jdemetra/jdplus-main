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
package jdplus.toolkit.base.xml.information;

import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.xml.legacy.IXmlConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlTsData.RNAME)
@XmlType(name = XmlTsData.NAME)
public class XmlTsData implements IXmlConverter<TsData> {

    static final String NAME = "tsDataType";
    static final String RNAME = "tsData";
    /**
     *
     */
    @XmlElement
    public Integer freq;
    /**
     *
     */
    @XmlElement
    public Integer firstYear;
    /**
     *
     */
    @XmlElement
    public Integer firstPeriod;

    // Alternative storage for more general tsdata (high-frequency)
    @XmlElement
    public XmlTsUnit unit;

    @XmlElement
    public String start;
    /**
     *
     */
    @XmlElement(name = "data")
    @XmlList
    public double[] data;

    @XmlAttribute
    public String name;

    /**
     *
     * @param t
     */
    @Override
    public void copy(TsData t) {
        TsPeriod tstart = t.getStart();
        int ifreq = tstart.getUnit().getAnnualFrequency();
        if (ifreq > 0) {
            freq=ifreq;
            firstYear = tstart.year();
            if (freq != 1) {
                firstPeriod = tstart.annualPosition() + 1;
            } else {
                firstPeriod = null;
            }
        } else {
            unit=new XmlTsUnit();
            unit.copy(tstart.getUnit());
            start = tstart.start().format(DateTimeFormatter.ISO_DATE_TIME);
        }
        if (!t.getValues().isEmpty()) {
            data = t.getValues().toArray();
        }
    }

    /**
     *
     * @return
     */
    @Override
    public TsData create() {
        if (freq != null) {
            return of(freq, firstYear, firstPeriod, data);
        } else {
            LocalDateTime tstart=LocalDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME);
            TsPeriod pstart=TsPeriod.of(unit.create(), tstart);
            return TsData.ofInternal(pstart, data == null ? Doubles.EMPTYARRAY : data);
        }
    }

    static TsData of(int freq, int year, int period, double[] data) {
        switch (freq) {
            case 1 -> {
                return TsData.ofInternal(TsPeriod.yearly(year), data);
            }
            case 12 -> {
                return TsData.ofInternal(TsPeriod.monthly(year, period), data);
            }
            default -> {
                int c = 12 / freq;
                TsPeriod pstart = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), LocalDate.of(year, (period - 1) * c + 1, 1));
                return TsData.ofInternal(pstart, data == null ? Doubles.EMPTYARRAY : data);
            }
        }

    }
}
