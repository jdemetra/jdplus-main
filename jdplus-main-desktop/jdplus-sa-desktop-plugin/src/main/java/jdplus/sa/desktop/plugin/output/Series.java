/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.sa.desktop.plugin.output;

import jdplus.toolkit.desktop.plugin.properties.ListSelectionEditor;
import jdplus.sa.base.api.SaManager;

/**
 *
 * @author PALATEJ
 */
public class Series extends ListSelectionEditor<String> {

    public Series() {
        super(OutputSelection.seriesItems(SaManager.processors()));
    }
   
   
}
