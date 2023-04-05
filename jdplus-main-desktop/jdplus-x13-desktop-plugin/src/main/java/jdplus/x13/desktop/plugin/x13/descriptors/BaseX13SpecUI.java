/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.desktop.plugin.x13.descriptors;

import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.x13.base.api.x11.X11Spec;

/**
 *
 * @author PALATEJ
 */
public abstract class BaseX13SpecUI implements IPropertyDescriptors{
    
    final X13SpecRoot root;
        
    BaseX13SpecUI(X13SpecRoot root){
        this.root =root;
    }
    
    RegArimaSpec regarima(){return root.getRegarima().getCore();}
    
    X11Spec x11(){return root.getX11();}
    
    boolean isRo(){return root.isRo();}
    
    void update(X11Spec nx11){
        root.update(nx11);
    }
}
