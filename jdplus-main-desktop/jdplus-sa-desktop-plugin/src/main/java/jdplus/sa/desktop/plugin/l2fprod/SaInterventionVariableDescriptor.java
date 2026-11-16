/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.l2fprod;

import java.util.ArrayList;
import java.util.List;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.InterventionVariableDescriptor;

/**
 *
 * @author Jean Palate
 */
public final class SaInterventionVariableDescriptor extends InterventionVariableDescriptor
        implements SaVariableDescriptor {

    public SaInterventionVariableDescriptor() {}

    private ComponentType regressionEffect;

    public SaInterventionVariableDescriptor(Variable<InterventionVariable> var) {
        super(var);
        ComponentType effect = SaVariable.regressionEffect(var);
        if (effect == ComponentType.Undefined) {
            effect = SaVariable.defaultComponentTypeOf(var.getCore());
        }
        regressionEffect = effect;
    }

    public SaInterventionVariableDescriptor(SaInterventionVariableDescriptor desc) {
        super(desc);
        this.regressionEffect = desc.regressionEffect;
    }

    @Override
    public SaInterventionVariableDescriptor duplicate() {
        return new SaInterventionVariableDescriptor(this);
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc;
        desc = nameDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = seqDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = deltaDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = deltaSDesc();
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

    private static final int SEQ_ID = 1, DELTA_ID = 2, DELTAS_ID = 3;

    @Override
    public String getDisplayName() {
        return "Intervention variable";
    }

    @Override
    public ComponentType getRegressionEffect() {
        return regressionEffect;
    }

    @Override
    public void setRegressionEffect(ComponentType type) {
        this.regressionEffect = type;
    }
}
