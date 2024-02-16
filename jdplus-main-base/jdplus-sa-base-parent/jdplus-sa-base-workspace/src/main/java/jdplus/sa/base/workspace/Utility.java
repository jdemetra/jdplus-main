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
package jdplus.sa.base.workspace;

import java.util.HashMap;
import jdplus.sa.base.api.SaDefinition;
import jdplus.sa.base.api.SaItem;
import jdplus.toolkit.base.api.timeseries.Ts;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Utility {

    public SaItem withTsMetaData(SaItem item, String key, String value) {
        SaDefinition definition = item.getDefinition();
        Ts cur = definition.getTs();
        HashMap<String, String> map = new HashMap<>(cur.getMeta());
        map.put(key, value);
        Ts ncur = cur.toBuilder().clearMeta()
                .meta(map)
                .build();
        SaDefinition ndef = definition.toBuilder()
                .ts(ncur)
                .build();
        return new SaItem(item.getName(), ndef, item.getMeta(), item.getPriority(), item.getEstimation(), item.isProcessed());
    }

    public String getSingleMetaData(SaItem item, String key) {
        return item.getMeta().get(key);
    }

    public String getSingleTsMetaData(SaItem item, String key) {
        Ts cur = item.getDefinition().getTs();
        return cur.getMeta().get(key);
    }

}
