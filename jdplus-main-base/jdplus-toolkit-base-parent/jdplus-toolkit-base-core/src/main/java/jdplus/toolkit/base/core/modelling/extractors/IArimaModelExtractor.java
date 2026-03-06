package jdplus.toolkit.base.core.modelling.extractors;

import jdplus.toolkit.base.api.dictionaries.ArimaDictionaries;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.arima.IArimaModel;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Christiane Hofer
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class IArimaModelExtractor extends InformationMapping<IArimaModel> {

    public IArimaModelExtractor() {
        set(ArimaDictionaries.AR, double[].class, source -> source.getStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray());
        set(ArimaDictionaries.DELTA, double[].class, source -> source.getNonStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray());
        set(ArimaDictionaries.MA, double[].class, source -> source.getMa().asPolynomial().coefficients().drop(1, 0).toArray());
        set(ArimaDictionaries.VAR, Double.class, source -> source.getInnovationVariance());
        set(ArimaDictionaries.NAME, String.class, source ->source.getClass().getName().replaceFirst(source.getClass().getPackageName()+".", ""));
    }

    @Override
    public Class getSourceClass() {
        return IArimaModel.class;
    }
}
