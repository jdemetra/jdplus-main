package jdplus.sa.desktop.plugin.multiprocessing.actions;

import java.util.ArrayList;
import java.util.List;
import jdplus.sa.base.api.SaSpecification;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.xml.information.XmlInformationSet;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.interchange.Importable;
import lombok.NonNull;
import nbbrd.io.text.Parser;
import nbbrd.io.xml.bind.Jaxb;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

@ServiceDefinition(quantifier = Quantifier.MULTIPLE)
public abstract class ImportableSpec<X extends SaSpecification> implements Importable {
    
    protected static final Parser<InformationSet> INFORMATIONPARSER = Jaxb.Parser.of(XmlInformationSet.class).asParser().andThen(XmlInformationSet::create);
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
        SaSpecification spec = fromConfig(config);
        if (spec != null) {
            ImportData data = new ImportData(spec, config.getName());
            LIST.add(data);
        }
    }

    protected abstract SaSpecification fromConfig(@NonNull Config config) throws IllegalArgumentException;
}
