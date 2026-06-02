package jdplus.x13.desktop.plugin.x13.ui.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.desktop.plugin.multiprocessing.actions.ImportableSpec;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.x13.base.information.X13SpecMapping;
import lombok.NonNull;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ImportableSpec.class)
public class ImportableX13Spec extends ImportableSpec<X13Spec> {

    public ImportableX13Spec() {
        super(X13Spec.class);
    }

    @Override
    protected SaSpecification fromConfig(@NonNull Config config) throws IllegalArgumentException {
        if (!getDomain().equals(config.getDomain())) {
            throw new IllegalArgumentException("Invalid config");
        }

        InformationSet set = INFORMATIONPARSER.parse(config.getParameter("specification"));
        
        SaSpecification xspec = X13SpecMapping.read(set, null);
        if (xspec != null) {
            return xspec;
        }

        Logger.getLogger(ImportableSpec.class.getName()).log(Level.SEVERE, "Mapping to spec failed.");
        return null;
    }
}
