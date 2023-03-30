/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.desktop.tramo.descriptors;

import jdplus.tramoseats.base.api.tramo.TramoSpec;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
@lombok.AllArgsConstructor
public class TramoSpecRoot  {
    
    TramoSpec core;
    boolean ro;
}
