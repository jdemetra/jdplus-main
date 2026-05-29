package jdplus.sa.desktop.plugin.multiprocessing.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.base.information.SaSpecificationMapping;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.xml.information.XmlInformationSet;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.interchange.Importable;
import lombok.NonNull;
import nbbrd.io.text.Parser;
import nbbrd.io.xml.bind.Jaxb;
import org.openide.util.Lookup;

public class ImportableSpec<X extends SaSpecification> implements Importable {

    private static final Parser<InformationSet> INFORMATIONPARSER = Jaxb.Parser.of(XmlInformationSet.class).asParser().andThen(XmlInformationSet::create);
    protected static final List<ImportData> LIST = new ArrayList<>();
    protected final Class<X> clazz;

    public ImportableSpec(Class<X> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getDomain() {
        return clazz.getName();
    }

    @Override
    public void importConfig(Config config) throws IllegalArgumentException {
        X spec = fromConfig(config);
        if (spec != null) {
            ImportData data = new ImportData(spec, config.getName());
            LIST.add(data);
        }
    }

    private X fromConfig(@NonNull Config config) throws IllegalArgumentException {
        if (!getDomain().equals(config.getDomain())) {
            throw new IllegalArgumentException("Invalid config");
        }
        
        InformationSet set = INFORMATIONPARSER.parse(config.getParameter("specification"));
        X xspec;
        for (SaSpecificationMapping mapping : Lookup.getDefault().lookupAll(SaSpecificationMapping.class)) {
            xspec = (X) mapping.read(set, null);
            if (xspec != null) {
                return xspec;
            }
        }
        Logger.getLogger(ImportableSpec.class.getName()).log(Level.SEVERE, "Mapping to spec failed.");
        return null;
    }

}
