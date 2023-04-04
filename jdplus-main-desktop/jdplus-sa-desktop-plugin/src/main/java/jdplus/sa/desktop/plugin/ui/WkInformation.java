/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdplus.sa.desktop.plugin.ui;

import jdplus.sa.base.api.ComponentDescriptor;
import jdplus.toolkit.base.core.ucarima.WienerKolmogorovEstimators;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class WkInformation {
    WienerKolmogorovEstimators estimators;
    ComponentDescriptor[] descriptors;
    int frequency;
}
