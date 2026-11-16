/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;

/**
 *
 * @author Jean Palate
 */
public class RampDescriptor extends VariableDescriptor {

    private String name;
    private Parameter p;
    private Ramp core;

    public RampDescriptor() {
        core = new Ramp(LocalDateTime.now(Clock.systemDefaultZone()), LocalDateTime.now(Clock.systemDefaultZone()));
        p = Parameter.undefined();
        name = core.description(null);
    }

    public RampDescriptor duplicate() {
        return new RampDescriptor(this);
    }

    public RampDescriptor(Variable<Ramp> ramp) {
        this.core = ramp.getCore();
        this.name = ramp.getName();
        this.p = ramp.getCoefficient(0);
    }

    public RampDescriptor(RampDescriptor desc) {
        this.core = desc.core;
        this.name = desc.name;
        this.p = desc.p;
    }

    @Override
    public Ramp getCore() {
        return core;
    }

    public LocalDate getStart() {
        return core.getStart().toLocalDate();
    }

    public void setStart(LocalDate day) {
        core = new Ramp(day.atStartOfDay(), core.getEnd());
    }

    public LocalDate getEnd() {
        return core.getEnd().toLocalDate();
    }

    public void setEnd(LocalDate day) {
        core = new Ramp(core.getStart(), day.atStartOfDay());
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc;
        desc = nameDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = startDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = endDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = parameterDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    private static final int START_ID = 10, END_ID = 11;

    private EnhancedPropertyDescriptor startDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("start", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, START_ID);
            desc.setDisplayName("Start");
            // edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor endDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("end", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, END_ID);
            desc.setDisplayName("End");
            // edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        return "Ramp";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Parameter getCoefficient() {
        return p;
    }

    @Override
    public void setCoefficient(Parameter p) {
        this.p = p;
    }
}
