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
package jdplus.cruncher.batch;

import jdplus.toolkit.base.api.processing.Output;
import jdplus.sa.base.api.SaItem;
import jdplus.sa.base.api.SaOutputFactory;
import jdplus.toolkit.base.api.util.LinearId;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class SaBundle {

    private final String name;
    private final Collection<SaItem> items;

    public SaBundle(String name, Collection<SaItem> items) {
        this.name = name;
        this.items = items;
    }

    public Collection<SaItem> getItems() {
        return items;
    }

    public void flush(List<SaOutputFactory> output, ISaBatchFeedback fb) {
        for (SaOutputFactory fac : output) {
            if (fac.isAvailable()) {
                Output cur = fac.create();
                try {
                    LinearId id = new LinearId(name);
                    cur.start(id);
                    for (SaItem item : items) {
                        cur.process(item.asDocument());
                    }
                    cur.end(id);
                    if (fb != null) {
                        fb.showItem(cur.getName(), "generated");
                    }
                } catch (Exception err) {
                    if (fb != null) {
                        fb.showItem(cur.getName(), "failed: " + err.getMessage());
                    }
                }
            }
        }
        for (SaItem item : items) {
            item.flush();
        }
        System.gc();
    }
}
