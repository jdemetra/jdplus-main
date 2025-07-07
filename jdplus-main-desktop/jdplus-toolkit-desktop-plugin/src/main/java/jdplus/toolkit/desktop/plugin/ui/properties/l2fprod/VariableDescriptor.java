/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import org.openide.util.NbBundle;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public abstract class VariableDescriptor<T extends ITsVariable> implements IObjectDescriptor<T> {

    private String name;
    private Parameter coefficient;

    @Override
    public String toString() {
        return getName();
    }

    protected VariableDescriptor() {
        name = null;
        coefficient = Parameter.undefined();
    }
    
    public abstract String name();

    protected VariableDescriptor(Variable<T> var) {
        this.coefficient = var.getCoefficient(0);
        this.name=null;
    }

    protected VariableDescriptor(VariableDescriptor<T> desc) {
        this.name = desc.name;
        this.coefficient = desc.coefficient;
    }

   public String getName() {
        return name == null ? name() : name;
    }

    public void setName(String name) {
        if (!name.isBlank() && !name.equals(name())) {
            this.name = name;
        }else
            this.name=null;
    }

    public Parameter getCoefficient() {
        return coefficient;
    }

    public Parameter getParameter() {
        return coefficient;
    }

    public void setParameter(Parameter p) {
        coefficient = p;
    }


    private static final int NAME_ID = 1, PARAMETER_ID = 2;

    @NbBundle.Messages("variableDescriptor.nameDesc.display=Name")
    protected EnhancedPropertyDescriptor nameDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("name", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, NAME_ID);
            desc.setDisplayName(Bundle.variableDescriptor_nameDesc_display());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages("variableDescriptor.paramDesc.display=Coefficient")
    protected EnhancedPropertyDescriptor parameterDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("parameter", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PARAMETER_ID);
            desc.setDisplayName(Bundle.variableDescriptor_paramDesc_display());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    public static String toShortString(LocalDateTime start, LocalDateTime end, TsDomain domain) {
        int period = domain == null ? 0 : domain.getAnnualFrequency();
        StringBuilder builder = new StringBuilder();
        if (period <= 0) {
            builder.append(start.toLocalDate().format(DateTimeFormatter.ISO_DATE))
                    .append(" - ").append(end.toLocalDate().format(DateTimeFormatter.ISO_DATE));
        } else {
            TsUnit unit = TsUnit.ofAnnualFrequency(period);
            TsPeriod pstart = TsPeriod.of(unit, start);
            TsPeriod pend = TsPeriod.of(unit, end);
            builder.append(pstart.getStartAsShortString())
                    .append(" - ").append(pend.getStartAsShortString());

        }
        return builder.toString();
    }

}
