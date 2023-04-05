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
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.base.api.information.BasicInformationExtractor;
import jdplus.toolkit.base.api.processing.ProcDocument;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author palatej
 * @param <D>
 */
@lombok.AllArgsConstructor
@lombok.experimental.FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class ContextualIds<D extends ProcDocument> {

    private String[] ids;
    private D document;

    public Ts makeTs(int idx, boolean fullname) {
        TsMoniker moniker = TsDynamicProvider.monikerOf(document, ids[idx]);
        Ts x = TsFactory.getDefault().makeTs(moniker, TsInformationType.All);
        if (!fullname) {
            String s = ids[idx];
            int j = s.lastIndexOf(BasicInformationExtractor.SEP);
            if (j > 0) {
                x = x.withName(s.substring(j + 1));

            }
        }
        return x;
    }

    public List<Ts> makeAllTs(boolean fullname) {

        List<Ts> items = new ArrayList<>();
        for (int i = 0; i < ids.length; ++i) {
            Ts x = makeTs(i, fullname);
            if (!x.getData().isEmpty()) {
                items.add(x);
            }
        }
        return items;
    }

    public int size() {
        return ids.length;
    }

    public String id(int idx) {
        return ids[idx];
    }

}
