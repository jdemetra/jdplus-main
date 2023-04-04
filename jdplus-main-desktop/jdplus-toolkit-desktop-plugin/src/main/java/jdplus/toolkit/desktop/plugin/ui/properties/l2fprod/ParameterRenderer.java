/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import jdplus.toolkit.base.api.data.Parameter;
import java.text.DecimalFormat;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author palatej
 */
public class ParameterRenderer extends DefaultTableCellRenderer {
    
    private static final DecimalFormat fmt;
    
    static {
        fmt = new DecimalFormat();
        fmt.setMaximumFractionDigits(6);
    }
    
    @Override
    protected void setValue(Object value) {
        if (value instanceof Parameter p && p.isDefined()) {
            setText(fmt.format(p.getValue()));
        } else {
            setText("");
        }
        
    }
    
}