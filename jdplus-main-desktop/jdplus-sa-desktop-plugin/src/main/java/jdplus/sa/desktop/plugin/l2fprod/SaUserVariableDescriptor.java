/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.l2fprod;

import java.util.ArrayList;
import java.util.List;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.api.timeseries.regression.UserVariableSpec;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserVariableDescriptor;

/**
 *
 * @author Jean Palate
 */
public class SaUserVariableDescriptor extends UserVariableDescriptor implements SaVariableDescriptor {

    public static final String DISPLAYNAME = "Ts variable";

    @Override
    public String toString() {
        return getName();
    }

    public SaUserVariableDescriptor() {}

    public SaUserVariableDescriptor(UserVariableSpec var) {
        super(var);
    }

    public SaUserVariableDescriptor(SaUserVariableDescriptor desc) {
        super(desc);
    }

    @Override
    public SaUserVariableDescriptor duplicate() {
        return new SaUserVariableDescriptor(this);
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
        desc = regDesc();
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

    @Override
    public String getDisplayName() {
        return DISPLAYNAME;
    }

    @Override
    public ComponentType getRegressionEffect() {
        String e = core.attribute(SaVariable.REGEFFECT);
        if (e == null) {
            return ComponentType.Undefined;
        } else {
            try {
                return ComponentType.valueOf(e);
            } catch (IllegalArgumentException err) {
                return ComponentType.Undefined;
            }
        }
    }

    @Override
    public void setRegressionEffect(ComponentType type) {
        core = core.setAttribute(SaVariable.REGEFFECT, type.name());
    }
}
