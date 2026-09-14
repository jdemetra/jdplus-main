/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.regarima.descriptors;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.desktop.plugin.descriptors.DateSelectorUI;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserInterfaceContext;
import jdplus.x13.base.api.regarima.X13Frequency;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jean Palate
 */
public class BasicSpecUI extends BaseRegArimaSpecUI {

    public BasicSpecUI(RegArimaSpecRoot root) {
        super(root);
    }

    public X13Frequency getFrequency() {
        return X13Frequency.parse(core().getFrequency());
    }

    public void setFrequency(X13Frequency freq) {
        int ifreq = freq.toInt();
        if (ifreq < 0) throw new IllegalArgumentException("Can't be used. For legacy purposes only");
        update(ifreq);
        UserInterfaceContext.INSTANCE.setAnnualFrequency(ifreq);
    }

    public DateSelectorUI getSpan() {
        return new DateSelectorUI(
                core().getBasic().getSpan(),
                UserInterfaceContext.INSTANCE.getDomain(),
                isRo(),
                selector -> updateSpan(selector));
    }

    public void updateSpan(TimeSelector span) {
        update(core().getBasic().toBuilder().span(span).build());
    }

    public boolean isPreprocessing() {
        return core().getBasic().isPreprocessing();
    }

    public void setPreprocessing(boolean pc) {
        if (pc != isPreprocessing()) {
            update(core().getBasic().toBuilder().preprocessing(pc).build());
        }
    }

    public boolean isPreliminaryCheck() {
        return core().getBasic().isPreliminaryCheck();
    }

    public void setPreliminaryCheck(boolean value) {
        update(core().getBasic().toBuilder().preliminaryCheck(value).build());
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = freqDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = spanDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = pcDesc();
        if (desc != null) {
            descs.add(desc);
        }
        //      excluded for the moment. That could change in the future
        //        desc = preprocessingDesc();
        //        if (desc != null) {
        //            descs.add(desc);
        //        }
        return descs;
    }

    @Override
    @Messages("basicSpecUI.getDislayName=Basic")
    public String getDisplayName() {
        return Bundle.basicSpecUI_getDislayName();
    }
    ///////////////////////////////////////////////////////////////////////////
    private static final int SPAN_ID = 1, AUTOMDL_ID = 2, PRELIMINARYCHECK_ID = 3, FREQ_ID = 0;

    @Messages({"basicSpecUI.freqDesc.name=Frequency", "basicSpecUI.freqDesc.desc=Number of periods in one year"})
    private EnhancedPropertyDescriptor freqDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("frequency", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FREQ_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(Bundle.basicSpecUI_freqDesc_desc());
            desc.setDisplayName(Bundle.basicSpecUI_freqDesc_name());
            edesc.setReadOnly(isRo() || UserInterfaceContext.INSTANCE.getDomain() != null);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"basicSpecUI.spanDesc.name=Series span", "basicSpecUI.spanDesc.desc=Time span used for the processing"})
    private EnhancedPropertyDescriptor spanDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("span", this.getClass(), "getSpan", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPAN_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(Bundle.basicSpecUI_spanDesc_desc());
            desc.setDisplayName(Bundle.basicSpecUI_spanDesc_name());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"basicSpecUI.automdlDesc.name=Preprocessing", "basicSpecUI.automdlDesc.desc=Preprocessing"})
    private EnhancedPropertyDescriptor preprocessingDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("preprocessing", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, AUTOMDL_ID);
            desc.setDisplayName(Bundle.basicSpecUI_automdlDesc_name());
            desc.setShortDescription(Bundle.basicSpecUI_automdlDesc_desc());
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "basicSpecUI.pcDesc.name=Preliminary Check",
        "basicSpecUI.pcDesc.desc=Checks that the series doesn't contain too many missing or identical values. In such a case, it is not processed"
    })
    private EnhancedPropertyDescriptor pcDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("preliminaryCheck", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PRELIMINARYCHECK_ID);
            desc.setDisplayName(Bundle.basicSpecUI_pcDesc_name());
            desc.setShortDescription(Bundle.basicSpecUI_pcDesc_desc());
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }
}
