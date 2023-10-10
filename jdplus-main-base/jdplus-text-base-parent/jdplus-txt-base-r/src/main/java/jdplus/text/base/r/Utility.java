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
package jdplus.text.base.r;

import java.util.Locale;
import jdplus.text.base.api.TxtProvider;
import jdplus.text.base.api.XmlProvider;
import jdplus.toolkit.base.r.util.Providers;
import jdplus.toolkit.base.tsp.util.ObsFormat;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Utility {

    public void updateTsFactory() {
        TxtProvider txtProvider = TxtFiles.currentProvider();
        XmlProvider xmlProvider = XmlFiles.currentProvider();
        if (txtProvider == null) {
            if (xmlProvider == null) {
                Providers.updateTsFactory(new TxtProvider(), new XmlProvider());
            } else {
                Providers.updateTsFactory(new TxtProvider());
            }
        } else {
            if (xmlProvider == null) {
                Providers.updateTsFactory(new XmlProvider());
            }
        }
    }
    
    public ObsFormat obsFormat(String locale, String dateFmt, String numberFmt, boolean ignoreNumberGrouping) {
        ObsFormat.Builder builder = ObsFormat.builder().ignoreNumberGrouping(ignoreNumberGrouping);
        if (locale.length()>0) {
            builder.locale(Locale.forLanguageTag(locale));
        }
        if (dateFmt.length()>0) {
            builder.dateTimePattern(dateFmt);
        }
        if (numberFmt.length()>0) {
            builder.numberPattern(numberFmt);
        }
        return builder.build();

    }
    
}
