/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdplus.x13.desktop.plugin.regarima.descriptors;

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import org.openide.util.NbBundle.Messages;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class CalendarSpecUI extends BaseRegArimaSpecUI {

    @Override
    public String toString(){
        return "";
    }

    public CalendarSpecUI(RegArimaSpecRoot root){
        super(root);
    }

    public TradingDaysSpecUI getTradingDays(){
        return new TradingDaysSpecUI(root);
    }

    public void setTradingDays(TradingDaysSpecUI spec){

    }

    public EasterSpecUI getEaster(){
        return new EasterSpecUI(root);
    }

    public void setEaster(EasterSpecUI spec){

    }

    @Override
     public List<EnhancedPropertyDescriptor> getProperties() {
        // regression
        ArrayList<EnhancedPropertyDescriptor> descs=new ArrayList<>();
        EnhancedPropertyDescriptor desc=tdDesc();
        if (desc != null)
            descs.add(desc);
        desc=easterDesc();
        if (desc != null)
            descs.add(desc);
        return descs;
    }


    ///////////////////////////////////////////////////////////////////////////

    private static final int TD_ID=1, EASTER_ID=2;

    @Messages({
        "calendarSpecUI.tdDesc.name=tradingDays",
        "calendarSpecUI.tdDesc.desc=tradingDays"
    })
    private EnhancedPropertyDescriptor tdDesc(){
        try {
            PropertyDescriptor desc = new PropertyDescriptor("tradingDays", this.getClass());
            EnhancedPropertyDescriptor edesc=new EnhancedPropertyDescriptor(desc, TD_ID);
            desc.setDisplayName(Bundle.calendarSpecUI_tdDesc_name());
            desc.setShortDescription(Bundle.calendarSpecUI_tdDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "calendarSpecUI.easterDesc.name=easter",
        "calendarSpecUI.easterDesc.desc=easter"
    })
    private EnhancedPropertyDescriptor easterDesc(){
        try {
            PropertyDescriptor desc = new PropertyDescriptor("easter", this.getClass());
            EnhancedPropertyDescriptor edesc=new EnhancedPropertyDescriptor(desc, EASTER_ID);
            desc.setDisplayName(Bundle.calendarSpecUI_easterDesc_name());
            desc.setShortDescription(Bundle.calendarSpecUI_easterDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages("calendarSpecUI.getDisplayName=Calendar")
    public String getDisplayName() {
        return Bundle.calendarSpecUI_getDisplayName();
    }
}
