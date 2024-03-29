/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class TsVariableDescriptor extends VariableDescriptor<TsContextVariable> {

    public static final String DISPLAYNAME = "Ts variable";
    private TsContextVariable core;
    
    @Override
    public String toString(){
        return getName();
    }

    public TsVariableDescriptor() {
        core = new TsContextVariable("");
    }

    public TsVariableDescriptor(Variable<TsContextVariable> var) {
        super(var);
        core = var.getCore();
        setName(var.getName());
    }
    
    public TsVariableDescriptor(TsVariableDescriptor desc) {
        super(desc);
        core = desc.core;
    }

    public TsVariableDescriptor duplicate(){
        return new TsVariableDescriptor(this);
    }

    public UserVariable getVariableName() {
        return new UserVariable(core.getId());
    }
    
    public void setVariableName(UserVariable name){
        core=core.withId(name.getName());
    }
    
    public int getLag(){
        return core.getLag();
    }

    public void setLag(int lag){
        core=core.withLag(lag);
    }

    @Override
    public TsContextVariable getCore() {
        return core;
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc;
        desc = nameDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = variableNameDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = lagDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = parameterDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    private static final int USERNAME_ID = 10, LAG_ID = 11;
    private static final String USERNAME = "Variable", LAG = "Lag";
    private static final String USERNAME_DESC = "Name", LAG_DESC = "Lag";

    private EnhancedPropertyDescriptor lagDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("firstLag", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LAG_ID);
            desc.setDisplayName(LAG);
            desc.setShortDescription(LAG_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor variableNameDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("userName", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, USERNAME_ID);
            desc.setDisplayName(USERNAME);
            desc.setShortDescription(USERNAME_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        return DISPLAYNAME;
    }

     @Override
    public String name() {
        return core.description(UserInterfaceContext.INSTANCE.getDomain());
    }
}
