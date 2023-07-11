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

import java.time.temporal.ChronoUnit;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.xml.legacy.IXmlConverter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import jdplus.toolkit.base.api.timeseries.TsUnit;

/**
 * 
 * @author Jean Palate
 */
@XmlType(name = XmlTsUnit.NAME)
public class XmlTsUnit implements IXmlConverter<TsUnit> {

    static final String NAME = "tsUnitType";
    /**
     *
     */
    @XmlElement
    public String unit;
    /**
     *
     */
    @XmlElement
    public int amount;
    /**
     * 
     * @param tunit
     */
    @Override
    public void copy(TsUnit tunit)
    {
	unit = tunit.getChronoUnit().name();
	amount = (int)tunit.getAmount();
    }

    /**
     * 
     * @return
     */
    @Override
    public TsUnit create()
    {
	return TsUnit.of(amount, ChronoUnit.valueOf(unit));
    }
}
