/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.modelling;

import jdplus.toolkit.desktop.plugin.ui.processing.HtmlItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.util.Id;
import java.util.function.Function;
import jdplus.toolkit.base.core.regarima.tests.OneStepAheadForecastingTest;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlOneStepAheadForecastingTest;

/**
 *
 * @author PALATEJ
 * @param <D>
 */
public abstract class OutOfSampleTestFactory<D extends TsDocument<?, ?>>
        extends ProcDocumentItemFactory<D, HtmlElement> {

    protected OutOfSampleTestFactory(Class<D> documentType, Id id, Function<D, RegSarimaModel> extractor) {
        super(documentType, id, extractor.andThen(source -> {
            if (source == null) {
                return null;
            }
            int lback;
            int freq = source.getDescription().getSeries().getAnnualFrequency();
            lback = switch (freq) {
                case 12 -> 18;
                case 6 -> 9;
                case 4 -> 6;
                default -> 5;
            };
            RegSarimaComputer processor = RegSarimaComputer.builder().build();
            OneStepAheadForecastingTest test = OneStepAheadForecastingTest.of(source.regarima(), processor, lback);
            return new HtmlOneStepAheadForecastingTest(test);
        }), new HtmlItemUI());
    }
}
