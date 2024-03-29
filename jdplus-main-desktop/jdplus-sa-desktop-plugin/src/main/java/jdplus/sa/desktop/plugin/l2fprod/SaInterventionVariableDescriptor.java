/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.l2fprod;

import jdplus.toolkit.base.api.data.Range;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserInterfaceContext;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.Sequence;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public final class SaInterventionVariableDescriptor extends SaVariableDescriptor<InterventionVariable> {

    private InterventionVariable core;

    public SaInterventionVariableDescriptor() {
        core = InterventionVariable.empty();
    }
    
    @Override
    public ComponentType regressionEffect(){
        return SaVariable.defaultComponentTypeOf(core);
    }

    public SaInterventionVariableDescriptor(Variable<InterventionVariable> var) {
        super(var);
        core = var.getCore();
        setName(var.getName());
        setRegressionEffect(SaVariable.regressionEffect(var));
    }

    public SaInterventionVariableDescriptor(SaInterventionVariableDescriptor desc) {
        super(desc);
        core = desc.core;
    }

    public SaInterventionVariableDescriptor duplicate() {
        return new SaInterventionVariableDescriptor(this);
    }
    
    @Override
    public String name(){
        return core.description(UserInterfaceContext.INSTANCE.getDomain());
    }

    @Override
    public InterventionVariable getCore() {
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

    private static final int SEQ_ID = 1,
            DELTA_ID = 2, DELTAS_ID = 3;

    private EnhancedPropertyDescriptor seqDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("sequences", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SEQ_ID);
            desc.setDisplayName("Sequences");
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor deltaDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("delta", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, DELTA_ID);
            desc.setDisplayName("Delta");
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor deltaSDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("deltaS", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, DELTAS_ID);
            desc.setDisplayName("Seasonal delta");
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    public Sequence[] getSequences() {
        List<Range<LocalDateTime>> sequences = core.getSequences();
        Sequence[] seq = new Sequence[sequences.size()];
        int i = 0;
        for (Range<LocalDateTime> s : sequences) {
            seq[i++] = new Sequence(s.start().toLocalDate(), s.end().toLocalDate());
        }
        return seq;
    }

    public void setSequences(Sequence[] val) {
        InterventionVariable.Builder builder = core.toBuilder()
                .clearSequences();
        if (val != null) {
            for (int i = 0; i < val.length; ++i) {
                builder.sequence(Range.of(val[i].getStart().atStartOfDay(), val[i].getEnd().atStartOfDay()));
            }
        }
        core = builder.build();
    }

    public double getDelta() {
        return core.getDelta();
    }

    public void setDelta(double val) {
        core = core.toBuilder()
                .delta(val).build();
    }

    public double getDeltaS() {
        return core.getDeltaSeasonal();
    }

    public void setDeltaS(double val) {
        core = core.toBuilder()
                .deltaSeasonal(val).build();
    }

    @Override
    public String getDisplayName() {
        return "Intervention variable";
    }

}
