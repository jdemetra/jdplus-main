/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.timeseries.regression.UserVariableSpec;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;

/**
 *
 * @author Jean Palate
 */
public class UserVariableDescriptor extends VariableDescriptor {

    public static final String DISPLAYNAME = "Ts variable";
    protected UserVariableSpec core;

    @Override
    public String toString() {
        return core.getName();
    }

    public UserVariableDescriptor() {
        core = UserVariableSpec.EMPTY;
    }

    public UserVariableDescriptor(UserVariableSpec var) {
        core = var;
    }

    public UserVariableDescriptor(UserVariableDescriptor desc) {
        core = desc.core;
    }

    public UserVariableDescriptor duplicate() {
        return new UserVariableDescriptor(this);
    }

    public UserVariable getVariableName() {
        return new UserVariable(core.getId());
    }

    public void setVariableName(UserVariable name) {
        core = core.withId(name.getName());
    }

    public int getLag() {
        return core.getLag();
    }

    public void setLag(int lag) {
        core = core.withLag(lag);
    }

    @Override
    public UserVariableSpec getCore() {
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

    protected EnhancedPropertyDescriptor lagDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("lag", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LAG_ID);
            desc.setDisplayName(LAG);
            desc.setShortDescription(LAG_DESC);
            // edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    protected EnhancedPropertyDescriptor variableNameDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("userName", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, USERNAME_ID);
            desc.setDisplayName(USERNAME);
            desc.setShortDescription(USERNAME_DESC);
            // edesc.setReadOnly(true);
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
    public String getName() {
        return core.getName();
    }

    @Override
    public void setName(String name) {
        core = core.withName(name);
    }

    @Override
    public Parameter getCoefficient() {
        return core.getCoefficient();
    }

    @Override
    public void setCoefficient(Parameter p) {
        core = core.withCoefficient(p);
    }
}
