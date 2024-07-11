/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.sa.desktop.plugin.descriptors.highfreq;

import jdplus.toolkit.desktop.plugin.descriptors.DateSelectorUI;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.toolkit.base.api.modelling.highfreq.SeriesSpec;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author PALATEJ
 */
public abstract class AbstractSeriesSpecUI implements IPropertyDescriptors {

    @Override
    public String toString() {
        return "";
    }
    
    protected abstract HighFreqSpecUI root();

    protected abstract SeriesSpec spec();

    public DateSelectorUI getSpan() {
        return new DateSelectorUI(spec().getSpan(), root().isRo(), selector -> updateSpan(selector));
    }

    public void updateSpan(TimeSelector span) {
        root().update(spec().toBuilder().span(span).build());
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = spanDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    private static final int SPAN_ID = 1, LOG_ID = 2;

    @NbBundle.Messages({
        "seriesSpecUI.spanDesc.name=Series span",
        "seriesSpecUI.spanDesc.desc=Time span used for the processing"
    })
    private EnhancedPropertyDescriptor spanDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("span", this.getClass(), "getSpan", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPAN_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(Bundle.seriesSpecUI_spanDesc_desc());
            desc.setDisplayName(Bundle.seriesSpecUI_spanDesc_name());
            edesc.setReadOnly(root().isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }


    @Override
    @NbBundle.Messages("seriesSpecUI.getDisplayName=Series")
    public String getDisplayName() {
        return Bundle.seriesSpecUI_getDisplayName();
    }

}
