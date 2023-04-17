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
package internal.toolkit.base.workspace.file.xml.util;

import jdplus.toolkit.base.api.timeseries.ValidityPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.FixedWeekDay;
import jdplus.toolkit.base.xml.legacy.IXmlConverter;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.Locale;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlFixedWeekDay.NAME)
public class XmlFixedWeekDay extends AbstractXmlDay implements IXmlConverter<FixedWeekDay> {

    static final String NAME = "fixedWeekDayType";

    @XmlElement
    public String month;
    @XmlElement
    public String dayOfWeek;
    @XmlElement
    public int place;

    @Override
    public FixedWeekDay create() {
        return new FixedWeekDay(ofMonth(month), place, ofDay(dayOfWeek), getWeight(), ValidityPeriod.ALWAYS);
    }

    @Override
    public void copy(FixedWeekDay t) {
        place = t.getPlace();
        dayOfWeek = t.getDayOfWeek().name();
        month = Month.of(t.getMonth()).name();
        setWeight(t.getWeight());
    }

    private static int ofMonth(String month) {
        String umonth=month.toUpperCase(Locale.ROOT);
        Month m=Month.valueOf(umonth);
        return m.getValue();
    }

    private static DayOfWeek ofDay(String day) {
        String uday=day.toUpperCase(Locale.ROOT);
        return DayOfWeek.valueOf(uday);
    }
}
