/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.descriptors.regular;

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.DateSelectorUI;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserInterfaceContext;
import jdplus.toolkit.base.api.modelling.regular.SeriesSpec;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jean Palate
 */
public abstract class AbstractSeriesSpecUI implements IPropertyDescriptors {
    
    protected abstract RegularSpecUI root();

    protected abstract SeriesSpec spec();
    


    public DateSelectorUI getSpan() {
        return new DateSelectorUI(spec().getSpan(), UserInterfaceContext.INSTANCE.getDomain(), root().isRo(), selector->updateSpan(selector));
    }
    
    public void updateSpan(TimeSelector span){
        root().update(spec().toBuilder().span(span).build());
    }

    public boolean isPreliminaryCheck() {
        return spec().isPreliminaryCheck();
    }

    public void setPreliminaryCheck(boolean value) {
        root().update(spec().toBuilder()
                .preliminaryCheck(value)
                .build());
    }

   public boolean isPreprocessing(){
       return root().isPreprocessing();
   }
   
   public void setPreprocessing(boolean enabled){
       root().update(root().preprocessing().toBuilder().enabled(enabled).build());
   }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = spanDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = pcDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = preprocessingDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    @Override
    @Messages("regular.seriesSpecUI.getDislayName=SERIES")
    public String getDisplayName() {
        return Bundle.regular_seriesSpecUI_getDislayName();
    }
    ///////////////////////////////////////////////////////////////////////////
    private static final int SPAN_ID = 1, PREPROCESSING_ID = 2, PRELIMINARYCHECK_ID = 3;

    @Messages({
        "regular.seriesSpecUI.spanDesc.name=Series span",
        "regular.seriesSpecUI.spanDesc.desc=Time span used for the processing"
    })
    private EnhancedPropertyDescriptor spanDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("span", this.getClass(), "getSpan", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPAN_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(Bundle.regular_seriesSpecUI_spanDesc_desc());
            desc.setDisplayName(Bundle.regular_seriesSpecUI_spanDesc_name());
            edesc.setReadOnly(root().isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "regular.seriesSpecUI.pcDesc.name=Preliminary Check",
        "regular.seriesSpecUI.pcDesc.desc=Checks that the series doesn't contain too many missing or identical values. In such a case, it is not processed"
    })
    private EnhancedPropertyDescriptor pcDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("preliminaryCheck", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PRELIMINARYCHECK_ID);
            desc.setDisplayName(Bundle.regular_seriesSpecUI_pcDesc_name());
            desc.setShortDescription(Bundle.regular_seriesSpecUI_pcDesc_desc());
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            edesc.setReadOnly(root().isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }
    
    @Messages({"seriesSpecUI.preprocessingDesc.name=PREPROCESSING",
        "seriesSpecUI.preprocessingDesc.desc=Reg-Arima (airline) preprocessing"
    })
    private EnhancedPropertyDescriptor preprocessingDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("preprocessing", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PREPROCESSING_ID);
            desc.setDisplayName(Bundle.seriesSpecUI_preprocessingDesc_name());
            desc.setShortDescription(Bundle.seriesSpecUI_preprocessingDesc_desc());
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }
  

}
