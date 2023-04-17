/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import jdplus.toolkit.base.api.data.Parameter;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author palatej
 */
public class ParameterRenderer extends DefaultTableCellRenderer {
    
    private static final NumberFormat fmt;
    
    static {
        fmt = NumberFormat.getNumberInstance(Locale.getDefault(Locale.Category.DISPLAY));
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
