/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.tramoseats.desktop.plugin.tramo.ui.actions;

import jdplus.sa.desktop.plugin.multiprocessing.actions.ImportableSpec;
import jdplus.sa.desktop.plugin.multiprocessing.actions.ImportableSpecFactory;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ImportableSpecFactory.class)
public class ImportableTramoSpecFactory  implements ImportableSpecFactory{

    @Override
    public ImportableSpec create() {
        return new ImportableSpec(TramoSeatsSpec.class);
    }
    
}
