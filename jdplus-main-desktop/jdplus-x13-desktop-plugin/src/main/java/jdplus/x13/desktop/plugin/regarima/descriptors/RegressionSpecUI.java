/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.regarima.descriptors;

import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaVariable;
import jdplus.sa.desktop.plugin.l2fprod.SaInterventionVariableDescriptor;
import jdplus.sa.desktop.plugin.l2fprod.SaTsVariableDescriptor;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.*;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.OutlierDescriptor;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.RampDescriptor;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserInterfaceContext;
import jdplus.x13.base.api.regarima.RegressionSpec;
import org.openide.util.NbBundle.Messages;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 */
public class RegressionSpecUI extends BaseRegArimaSpecUI {

    private static IOutlier toOutlier(OutlierDescriptor od, int period, double tc) {
        return switch (od.getType()) {
            case AO ->
                new AdditiveOutlier(od.getPosition().atStartOfDay());
            case LS ->
                new LevelShift(od.getPosition().atStartOfDay(), true);
            case TC ->
                new TransitoryChange(od.getPosition().atStartOfDay(), tc);
            case SO ->
                new PeriodicOutlier(od.getPosition().atStartOfDay(), period, true);
            default ->
                null;
        };
    }

    private RegressionSpec inner() {
        return core().getRegression();
    }

    public RegressionSpecUI(RegArimaSpecRoot root) {
        super(root);
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        // regression
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = meanDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = calendarDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = prespecDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = interventionDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = rampsDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = userdefinedDesc();
        if (desc != null) {
            descs.add(desc);
        }

//        desc = fixedCoefficientsDesc();
//        if (desc != null) {
//            descs.add(desc);
//        }
        return descs;
    }

    @Override
    @Messages("regressionSpecUI.getDisplayName=Regression")
    public String getDisplayName() {
        return Bundle.regressionSpecUI_getDisplayName();
    }

    public Parameter[] parameter(Parameter p) {
        if (!isTransformationDefined()) {
            p = Parameter.undefined();
        }
        return new Parameter[]{p};
    }

    public OutlierDescriptor[] getPreSpecifiedOutliers() {
        return inner().getOutliers()
                .stream()
                .map(var -> {
                    IOutlier o = var.getCore();
                    return new OutlierDescriptor(o.getPosition().toLocalDate(), OutlierDescriptor.OutlierType.valueOf(o.getCode()), var.getCoefficient(0), var.getName());
                })
                .sorted((o1, o2) -> o1.getPosition().compareTo(o2.getPosition()))
                .toArray(n -> new OutlierDescriptor[n]);
    }

    public void setPreSpecifiedOutliers(OutlierDescriptor[] value) {

        double tc = core().getOutliers().getMonthlyTCRate();
        TsDomain domain = UserInterfaceContext.INSTANCE.getDomain();
        int period = domain == null ? 0 : domain.getAnnualFrequency();
        List<Variable<IOutlier>> list = Arrays.stream(value).map(v -> Variable.<IOutlier>builder()
                .name(v.getName())
                .core(toOutlier(v, period, tc))
                .coefficients(parameter(v.getCoefficient()))
                .build())
                .collect(Collectors.toList());
        update(inner().toBuilder().clearOutliers().outliers(list).build());
    }

    public SaInterventionVariableDescriptor[] getInterventionVariables() {
        return inner().getInterventionVariables()
                .stream()
                .map(var -> new SaInterventionVariableDescriptor(var))
                .toArray(SaInterventionVariableDescriptor[]::new);
    }

    public void setInterventionVariables(SaInterventionVariableDescriptor[] value) {
        List<Variable<InterventionVariable>> list = Arrays.stream(value).map(v -> Variable.<InterventionVariable>builder()
                .name(v.getName())
                .core(v.getCore())
                .attribute(SaVariable.REGEFFECT, v.getRegressionEffect().name())
                .coefficients(parameter(v.getCoefficient()))
                .build())
                .collect(Collectors.toList());
        update(inner().toBuilder().clearInterventionVariables().interventionVariables(list).build());
    }

    public RampDescriptor[] getRamps() {
        return inner().getRamps()
                .stream()
                .map(var -> new RampDescriptor(var))
                .toArray(RampDescriptor[]::new);
    }

    public void setRamps(RampDescriptor[] value) {
        List<Variable<Ramp>> list = Arrays.stream(value).map(v -> Variable.<Ramp>builder()
                .name(v.getName())
                .core(v.getCore())
                .attribute(SaVariable.REGEFFECT, ComponentType.Trend.name())
                .coefficients(parameter(v.getCoefficient()))
                .build())
                .collect(Collectors.toList());
        update(inner().toBuilder().clearRamps().ramps(list).build());
    }

    public SaTsVariableDescriptor[] getUserDefinedVariables() {
        return inner().getUserDefinedVariables()
                .stream()
                .map(var -> new SaTsVariableDescriptor(var))
                .toArray(SaTsVariableDescriptor[]::new);
    }

    public void setUserDefinedVariables(SaTsVariableDescriptor[] value) {
        List<Variable<TsContextVariable>> list = Arrays.stream(value).map(v -> Variable.<TsContextVariable>builder()
                .name(v.getName())
                .core(v.getCore())
                .coefficients(parameter(v.getCoefficient()))
                .attribute(SaVariable.REGEFFECT, v.getRegressionEffect().name())
                .build())
                .collect(Collectors.toList());
        update(inner().toBuilder().clearUserDefinedVariables().userDefinedVariables(list).build());
    }

    public MeanSpecUI getMean() {
        return new MeanSpecUI(root);
    }

    private static final int MEAN_ID = 0, CALENDAR_ID = 2, PRESPEC_ID = 3, INTERV_ID = 4, RAMPS_ID = 5, USERDEF_ID = 6, FCOEFF_ID = 7;

    @Messages({
        "regressionSpecUI.meanDesc.desc=[imean] Mean correction"
    })
    private EnhancedPropertyDescriptor meanDesc() {
        if (core().isUsingAutoModel()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Mean", this.getClass(), "getMean", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, MEAN_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(Bundle.regressionSpecUI_meanDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }


    @Messages({
        "regressionSpecUI.prespecDesc.name=Pre-specified outliers",
        "regressionSpecUI.prespecDesc.desc=Pre-specified outliers"
    })
    private EnhancedPropertyDescriptor prespecDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("PreSpecifiedOutliers", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PRESPEC_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.regressionSpecUI_prespecDesc_name());
            desc.setShortDescription(Bundle.regressionSpecUI_prespecDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "regressionSpecUI.interventionDesc.name=Intervention variables",
        "regressionSpecUI.interventionDesc.desc=Intervention variables"
    })
    private EnhancedPropertyDescriptor interventionDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("InterventionVariables", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, INTERV_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.regressionSpecUI_interventionDesc_name());
            desc.setShortDescription(Bundle.regressionSpecUI_interventionDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "regressionSpecUI.rampsDesc.name=Ramps",
        "regressionSpecUI.rampsDesc.desc=Ramps"
    })
    private EnhancedPropertyDescriptor rampsDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Ramps", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, RAMPS_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.regressionSpecUI_rampsDesc_name());
            desc.setShortDescription(Bundle.regressionSpecUI_rampsDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "regressionSpecUI.userdefinedDesc.name=User-defined variables",
        "regressionSpecUI.userdefinedDesc.desc=User-defined variables"
    })
    private EnhancedPropertyDescriptor userdefinedDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("UserDefinedVariables", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, USERDEF_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.regressionSpecUI_userdefinedDesc_name());
            desc.setShortDescription(Bundle.regressionSpecUI_userdefinedDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

//    @Messages({
//        "regressionSpecUI.fixedCoefficientsDesc.name=Fixed regression coefficients",
//        "regressionSpecUI.fixedCoefficientsDesc.desc="
//    })
//    private EnhancedPropertyDescriptor fixedCoefficientsDesc() {
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("FixedCoefficients", this.getClass());
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FCOEFF_ID);
//            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
//            desc.setDisplayName(Bundle.regressionSpecUI_fixedCoefficientsDesc_name());
//            desc.setShortDescription(Bundle.regressionSpecUI_fixedCoefficientsDesc_desc());
//            // Disabled when the transformation is on "auto"
//            edesc.setReadOnly(isRo() || core().getTransform().getFunction() == TransformationType.Auto);
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }
    @Messages({
        "regressionSpecUI.calendarDesc.name=Calendar",
        "regressionSpecUI.calendarDesc.desc=Calendar effects"
    })
    private EnhancedPropertyDescriptor calendarDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("calendar", this.getClass(), "getCalendar", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, CALENDAR_ID);
            desc.setDisplayName(Bundle.regressionSpecUI_calendarDesc_name());
            desc.setShortDescription(Bundle.regressionSpecUI_calendarDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    public CalendarSpecUI getCalendar() {
        return new CalendarSpecUI(root);
    }
}
