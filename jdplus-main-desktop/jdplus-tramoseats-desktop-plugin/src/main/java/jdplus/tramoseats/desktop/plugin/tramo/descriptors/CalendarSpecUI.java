/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdplus.tramoseats.desktop.plugin.tramo.descriptors;

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import org.openide.util.NbBundle.Messages;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class CalendarSpecUI extends BaseTramoSpecUI{

    @Override
    public String toString(){
        return "";
    }
   
    public CalendarSpecUI(TramoSpecRoot root){
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
        "calendarSpecUI.tdDesc.name=Trading days",
        "calendarSpecUI.tdDesc.desc=Trading days"
    })
    private EnhancedPropertyDescriptor tdDesc(){
        try {
            PropertyDescriptor desc = new PropertyDescriptor("tradingDays", this.getClass());
            EnhancedPropertyDescriptor edesc=new EnhancedPropertyDescriptor(desc, TD_ID);
            desc.setDisplayName(Bundle.calendarSpecUI_tdDesc_name());
            desc.setShortDescription(Bundle.calendarSpecUI_tdDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "calendarSpecUI.easterDesc.name=Easter",
        "calendarSpecUI.easterDesc.desc=Easter"
    })
    private EnhancedPropertyDescriptor easterDesc(){
        try {
            PropertyDescriptor desc = new PropertyDescriptor("easter", this.getClass());
            EnhancedPropertyDescriptor edesc=new EnhancedPropertyDescriptor(desc, EASTER_ID);
            desc.setDisplayName(Bundle.calendarSpecUI_easterDesc_name());
            desc.setShortDescription(Bundle.calendarSpecUI_easterDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages("calendarSpecUI.getDisplayName=Calendar")
    @Override
    public String getDisplayName() {
        return Bundle.calendarSpecUI_getDisplayName();
    }
}
