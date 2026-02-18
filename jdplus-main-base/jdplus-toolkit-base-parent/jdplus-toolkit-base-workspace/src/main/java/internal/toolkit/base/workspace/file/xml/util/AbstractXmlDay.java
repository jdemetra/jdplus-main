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

import jdplus.toolkit.base.api.timeseries.calendars.EasterRelatedDay;
import jdplus.toolkit.base.api.timeseries.calendars.FixedDay;
import jdplus.toolkit.base.api.timeseries.calendars.FixedWeekDay;
import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import jdplus.toolkit.base.api.timeseries.calendars.PrespecifiedHoliday;
import jdplus.toolkit.base.api.timeseries.calendars.SingleDate;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Jean Palate
 */
public abstract class AbstractXmlDay {
    
    @XmlElement
    public Double weight;
    
    protected double getWeight(){
        return weight == null ? 1 : weight;
    }
    
    protected void setWeight(double w){
        if (w == 1)
            weight=null;
        else
            weight=w;
    }
    
    public abstract Holiday create();

    public static AbstractXmlDay convert(Holiday day)
    {
        if (day == null)
            return null;
        if (day instanceof FixedDay fixedDay)
        {
            XmlFixedDay fday=new XmlFixedDay();
            fday.copy(fixedDay);
            return fday;
        }
        if (day instanceof FixedWeekDay weekDay)
        {
            XmlFixedWeekDay fday=new XmlFixedWeekDay();
            fday.copy(weekDay);
            return fday;
        }
        if (day instanceof EasterRelatedDay relatedDay)
        {
            XmlEasterRelatedDay fday=new XmlEasterRelatedDay();
            fday.copy(relatedDay);
            return fday;
        }
        if (day instanceof PrespecifiedHoliday holiday)
        {
            XmlSpecialCalendarDay sday=new XmlSpecialCalendarDay();
            sday.copy(holiday);
            return sday;
        }
        if (day instanceof SingleDate date)
        {
            XmlSingleDate sday=new XmlSingleDate();
            sday.copy(date);
            return sday;
        }
        return null;
    }
}
