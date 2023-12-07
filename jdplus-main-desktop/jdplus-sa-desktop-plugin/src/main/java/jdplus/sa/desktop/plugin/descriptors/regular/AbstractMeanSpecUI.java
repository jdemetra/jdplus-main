/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.descriptors.regular;

import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import org.openide.util.NbBundle.Messages;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.modelling.regular.MeanSpec;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;

/**
 *
 * @author Jean Palate
 */
public abstract class AbstractMeanSpecUI implements IPropertyDescriptors {

    @Override
    public String toString() {
        return isUsed() ? "in use" : "";
    }

    protected abstract MeanSpec spec();

    protected abstract RegularSpecUI root();

    public boolean isUsed() {
        return spec().isUsed();
    }

    public void setUsed(boolean used) {
        if (used == isUsed()) {
            return;
        }
        if (!used) {
            root().update(MeanSpec.DEFAULT_UNUSED);
        } else {
            root().update(MeanSpec.DEFAULT_USED);
        }
    }

    public boolean isTest() {
        return spec().isTest();
    }

    public void setTest(boolean value) {
        root().update(spec().toBuilder().test(value).build());
    }

    public double getCoefficient() {
        Parameter coefficient = spec().getCoefficient();
        return coefficient != null && coefficient.isDefined() ? coefficient.getValue() : 0;
    }

    public void setCoefficient(double p) {
        Parameter coefficient = p == 0 ? Parameter.undefined() : Parameter.fixed(p);
        root().update(spec().toBuilder().coefficient(coefficient).build());
    }

    public boolean isFixedCoefficient() {
        Parameter coefficient = spec().getCoefficient();
        return coefficient != null && coefficient.isFixed();
    }

    public void setFixedCoefficient(boolean f) {
        Parameter coefficient = spec().getCoefficient();
        if (coefficient == null) {
            coefficient = Parameter.undefined();
        }
        if (f && !coefficient.isFixed()) {
            coefficient = Parameter.fixed(coefficient.getValue());
            root().update(spec().toBuilder().coefficient(coefficient).build());
        } else if (coefficient.isFixed()) {
            if (coefficient.getValue() == 0) {
                coefficient = Parameter.undefined();
            } else {
                coefficient = Parameter.initial(coefficient.getValue());
            }
            root().update(spec().toBuilder().coefficient(coefficient).build());
        }
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        // regression
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = usedDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = testDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = fixedCoeffDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = coeffDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    ///////////////////////////////////////////////////////////////////////////
    private static final int USED_ID = 1, TEST_ID = 3, COEFF_ID = 5, FIXED_COEFF_ID = 6;

    @Messages({
        "meanSpecUI.usedDesc.name=Trend constant",
        "meanSpecUI.usedDesc.desc="
    })
    private EnhancedPropertyDescriptor usedDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Used", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, USED_ID);
            desc.setDisplayName(Bundle.meanSpecUI_usedDesc_name());
            desc.setShortDescription(Bundle.meanSpecUI_usedDesc_desc());
            edesc.setReadOnly(root().isRo() || isFixedCoefficient());
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "meanSpecUI.testDesc.name=Test",
        "meanSpecUI.testDesc.desc="
    })
    private EnhancedPropertyDescriptor testDesc() {
        if (!isUsed()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("test", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TEST_ID);
            desc.setDisplayName(Bundle.meanSpecUI_testDesc_name());
            desc.setShortDescription(Bundle.meanSpecUI_testDesc_desc());
            edesc.setReadOnly(root().isRo() || isFixedCoefficient());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "meanSpecUI.coeffDesc.name=Coefficient",
        "meanSpecUI.coeffDesc.desc=Coefficient"
    })

    private EnhancedPropertyDescriptor coeffDesc() {
        if (!isFixedCoefficient()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("coefficient", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, COEFF_ID);
            desc.setDisplayName(Bundle.meanSpecUI_coeffDesc_name());
            desc.setShortDescription(Bundle.meanSpecUI_coeffDesc_desc());
            if (root().isRo()) {
                edesc.setReadOnly(true);
            }
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "meanSpecUI.fixedCoeffDesc.name=Fixed coeff.",
        "meanSpecUI.fixedCoeffDesc.desc=Fixed coeff."
    })
    private EnhancedPropertyDescriptor fixedCoeffDesc() {
        if (!isUsed()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("fixedCoefficient", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FIXED_COEFF_ID);
            desc.setDisplayName(Bundle.meanSpecUI_fixedCoeffDesc_name());
            desc.setShortDescription(Bundle.meanSpecUI_fixedCoeffDesc_desc());
            if (root().isRo() || !isUsed() || isTest() || !root().isTransformationDefined()) {
                edesc.setReadOnly(true);
            }
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Override
    @Messages("meanSpecUI.getDisplayName=Easter")
    public String getDisplayName() {
        return Bundle.meanSpecUI_getDisplayName();
    }
}
