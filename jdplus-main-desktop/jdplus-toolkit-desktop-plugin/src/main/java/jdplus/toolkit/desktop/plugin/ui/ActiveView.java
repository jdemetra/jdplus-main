/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui;

import javax.swing.JMenu;

/**
 *
 * @author Jean Palate
 */
public interface ActiveView {
    String getName();
    
    default boolean fill(JMenu menu){
        return false;
    }
    
    default boolean hasContextMenu(){
        return false;
    }
}
