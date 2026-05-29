/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.desktop.plugin.x13.ui.actions;

import jdplus.sa.desktop.plugin.multiprocessing.actions.ImportableSpec;
import jdplus.sa.desktop.plugin.multiprocessing.actions.ImportableSpecFactory;
import jdplus.x13.base.api.x13.X13Spec;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ImportableSpecFactory.class)
public class ImportableX13SpecFactory implements ImportableSpecFactory{

    @Override
    public ImportableSpec create() {
        return new ImportableSpec(X13Spec.class);
    }
    
}
