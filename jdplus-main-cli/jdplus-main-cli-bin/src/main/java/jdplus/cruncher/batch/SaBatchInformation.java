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

import jdplus.sa.base.api.SaItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class SaBatchInformation {
    private String name;
    private SaItem[] items;
    private int bundlesize;
    private SaBundle[] bundles;

    public SaBatchInformation(int bundlesize) {
        this.bundlesize = bundlesize;
    }

    public String getName() {
        return name;
    }
    public void setName(String value) {
        name = value;
    }

    public Iterable<SaItem> getItems() {
        return Arrays.asList(items);
    }
    
    public void setItems(Iterable<SaItem> value) {
        Iterator<SaItem> iter = value.iterator();
        List<SaItem> list = new ArrayList<>();
        while(iter.hasNext())
            list.add(iter.next());
        items = list.toArray(new SaItem[list.size()]);
    }

    public boolean open() {
        return true;
    }

    public Iterator<SaBundle> start() {

        if (bundlesize == 0)
            bundles = new SaBundle[] { new SaBundle(name, Arrays.asList(items)) };
        else {
            int n = items.length;
            int nb = 1 + (n - 1) / bundlesize;
            bundles = new SaBundle[nb];
            for (int i = 0, j = 0; i < nb; ++i, j += bundlesize) {
                String id = name;
                if (id == null)
                    id = "";
                StringBuilder builder = new StringBuilder();
                builder.append(id).append('_').append(i + 1);

                String m = builder.toString();
                SaItem[] items = new SaItem[Math.min(bundlesize, n - j)];
                for (int k = 0; k < items.length; ++k)
                    items[k] = this.items[j + k];
                bundles[i] = new SaBundle(m, Arrays.asList(items));
            }
        }
        Iterable<SaBundle> bundles = Arrays.asList(this.bundles);
        return bundles.iterator();
    }

    public void close() 
    {
    }
}
