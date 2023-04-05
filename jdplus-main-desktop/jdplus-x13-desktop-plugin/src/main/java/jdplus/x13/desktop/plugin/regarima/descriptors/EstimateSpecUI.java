/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.regarima.descriptors;

import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.desktop.plugin.descriptors.DateSelectorUI;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.x13.base.api.regarima.EstimateSpec;
import org.openide.util.NbBundle.Messages;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class EstimateSpecUI extends BaseRegArimaSpecUI {

    @Override
    public String toString() {
        return "";
    }
    
    private EstimateSpec inner(){
        return core().getEstimate();
    }
    
    public EstimateSpecUI(RegArimaSpecRoot root) {
        super(root);
     }

    public DateSelectorUI getSpan() {
        return new DateSelectorUI(inner().getSpan(), isRo(), selector->updateSpan(selector));
    }

     public void updateSpan(TimeSelector span){
        update(inner().toBuilder().span(span).build());
    }

    public double getTol() {
        return inner().getTol();
    }

    public void setTol(double value) {
        update(inner().toBuilder().tol(value).build());
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = spanDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = tolDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    @Messages({
        "estimateSpecUI.tolDesc.name=Tolerance",
        "estimateSpecUI.tolDesc.desc=[tol] Precision used in the optimization procedure."
    })
    private EnhancedPropertyDescriptor tolDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Tol", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TOL_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.estimateSpecUI_tolDesc_name());
            desc.setShortDescription(Bundle.estimateSpecUI_tolDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "estimateSpecUI.spanDesc.name=Model span",
        "estimateSpecUI.spanDesc.desc=Span used for the estimation of the pre-processing model"
    })
    private EnhancedPropertyDescriptor spanDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("span", this.getClass(), "getSpan", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPAN_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(Bundle.estimateSpecUI_spanDesc_desc());
            desc.setDisplayName(Bundle.estimateSpecUI_spanDesc_name());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }
    
    private static final int SPAN_ID = 0, TOL_ID = 1;

    @Override
    @Messages("estimateSpecUI.getDisplayName=Estimate")
    public String getDisplayName() {
        return Bundle.estimateSpecUI_getDisplayName();
    }
}

