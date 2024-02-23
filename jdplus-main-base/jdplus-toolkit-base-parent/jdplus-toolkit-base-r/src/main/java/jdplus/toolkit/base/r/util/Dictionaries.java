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
package jdplus.toolkit.base.r.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Dictionaries {

    public String[] entries(jdplus.toolkit.base.api.dictionaries.Dictionary dic) {
        return dic.entries().map(entry -> entry.fullName()).toArray(String[]::new);
    }

    /**
     * Output containing all the details, in the following order (for each
     * entry) name, description, detail, output; type, fullname
     *
     * @param dic
     * @return
     */
    public String[] all(jdplus.toolkit.base.api.dictionaries.Dictionary dic) {
        List<String> all = new ArrayList<>();
        dic.entries().forEachOrdered(entry -> {
            all.add(entry.getName());
            all.add(entry.getDescription());
            all.add(entry.getDetail());
            all.add(entry.getOutputClass().getCanonicalName());
            all.add(entry.getType().name());
            all.add(entry.fullName());

        }
        );
        return all.toArray(String[]::new);
    }

}
