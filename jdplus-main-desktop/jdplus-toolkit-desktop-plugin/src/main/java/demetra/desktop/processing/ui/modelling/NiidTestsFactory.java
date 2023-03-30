/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.desktop.processing.ui.modelling;

import demetra.desktop.ui.processing.HtmlItemUI;
import demetra.desktop.ui.processing.ProcDocumentItemFactory;
import demetra.html.HtmlElement;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.util.Id;
import java.util.function.Function;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.stats.tests.NiidTests;

/**
 *
 * @author PALATEJ
 * @param <D>
 */
public abstract class NiidTestsFactory<D extends TsDocument<?, ?>>
        extends ProcDocumentItemFactory<D, HtmlElement> {

    protected NiidTestsFactory(Class<D> documentType, Id id, Function<D, RegSarimaModel> extractor) {
        super(documentType, id, extractor.andThen(source -> {
            if (source == null) {
                return null;
            }
            TsData res = source.fullResiduals();
            NiidTests niid = NiidTests.builder()
                    .data(res.getValues())
                    .hyperParametersCount(source.freeArimaParametersCount())
                    .period(res.getAnnualFrequency())
                    .defaultTestsLength()
                    .build();
            return new demetra.html.stats.HtmlNiidTest(niid);
        }), new HtmlItemUI());
    }
}
