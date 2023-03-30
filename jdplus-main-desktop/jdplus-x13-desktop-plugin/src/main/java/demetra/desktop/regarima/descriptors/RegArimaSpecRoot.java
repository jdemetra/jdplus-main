/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.desktop.regarima.descriptors;

import jdplus.x13.base.api.regarima.RegArimaSpec;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
@lombok.AllArgsConstructor
public class RegArimaSpecRoot  {
    RegArimaSpec core;
    boolean ro;
}
