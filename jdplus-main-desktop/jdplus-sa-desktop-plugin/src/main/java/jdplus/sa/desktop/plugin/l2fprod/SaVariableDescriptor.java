/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.l2fprod;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import jdplus.sa.base.api.ComponentType;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;

/**
 *
 * @author Jean Palate
 */
public interface SaVariableDescriptor {

    public ComponentType getRegressionEffect();

    public void setRegressionEffect(ComponentType type);

    public static final int REGEFFECT_ID = 5;

    public default EnhancedPropertyDescriptor regDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("regressionEffect", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, REGEFFECT_ID);
            desc.setDisplayName("Regression effect");
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }
}
