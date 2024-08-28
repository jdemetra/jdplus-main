/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.descriptors;

import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.tramoseats.base.api.seats.DecompositionSpec;
import jdplus.tramoseats.base.api.tramo.TramoSpec;

/**
 *
 * @author PALATEJ
 */
public abstract class BaseTramoSeatsSpecUI implements IPropertyDescriptors{
    
    final TramoSeatsSpecRoot root;
        
    BaseTramoSeatsSpecUI(TramoSeatsSpecRoot root){
        this.root =root;
    }
    
    DecompositionSpec seats(){return root.getSeats();}
    
    TramoSpec tramo(){return root.getTramo().getCore();}

    boolean isRo(){return root.isRo();}
    
    void update(DecompositionSpec nseats){
        root.update(nseats);
    }
}
