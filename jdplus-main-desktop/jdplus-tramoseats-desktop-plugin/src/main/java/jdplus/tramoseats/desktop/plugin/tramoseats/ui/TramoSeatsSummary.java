/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.ui;

import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import javax.swing.JComponent;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDocument;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsSummary implements ItemUI<TramoSeatsDocument>{

    @Override
    public JComponent getView(TramoSeatsDocument document) {
        JTramoSeatsSummary view=new JTramoSeatsSummary();
        view.set(document);
        return view;
    }
    
}
