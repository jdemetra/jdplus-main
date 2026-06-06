package jdplus.tramoseats.desktop.plugin.tramo.ui.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.desktop.plugin.multiprocessing.actions.ImportableSpec;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.base.information.TramoSeatsSpecMapping;
import lombok.NonNull;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ImportableSpec.class)
public class ImportableTramoSeatsSpec extends ImportableSpec<TramoSeatsSpec>{
    public ImportableTramoSeatsSpec(){
        super(TramoSeatsSpec.class);
    }
    

    @Override
    protected SaSpecification fromConfig(@NonNull Config config) throws IllegalArgumentException {
        if (!getDomain().equals(config.getDomain())) {
            throw new IllegalArgumentException("Invalid config");
        }

        InformationSet set = INFORMATIONPARSER.parse(config.getParameter("specification"));

        SaSpecification xspec = TramoSeatsSpecMapping.read(set, null);
        if (xspec != null) {
            return xspec;
        }

        Logger.getLogger(ImportableSpec.class.getName()).log(Level.SEVERE, "Mapping to spec failed.");
        return null;
    }

}
