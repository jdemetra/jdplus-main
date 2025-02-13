/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.x13.base.information;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.sa.base.api.*;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.sa.base.information.SaItemMapping;
import jdplus.sa.base.information.SaItemsMapping;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.xml.information.XmlInformationSet;
import jdplus.toolkit.base.api.util.NameManager;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.x13.base.core.x13.X13Results;
import nbbrd.io.xml.bind.Jaxb;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

/**
 *
 * @author PALATEJ
 */
public class X13SpecMappingTest {

    public X13SpecMappingTest() {
    }

    @Test // Christiane Hofer
    public void testReadLegacy() {
        X13Spec specInput = X13Spec.builder().build();

        InformationSet infoV2 = X13SpecMapping.write(specInput, null, true);
        InformationSet infoV2X11 = infoV2.getSubSet(X13SpecMapping.X11);
        String[] vsig = {"Group1", "Group2", "Group1", "Group1"};
        infoV2X11.set(X11SpecMapping.SIGMAVEC, vsig);
        infoV2X11.remove(X11SpecMapping.FCASTS);// The default value could be 0 vor X11 or -1 for X13 this is not saved in the SA Processiong of Version 2

        X13Spec specV3 = X13SpecMapping.readLegacy(infoV2, null);
        org.junit.Assert.assertNull("Sigmavec is not null and Calendarsigma is default", specV3.getX11().getSigmaVec());
        org.junit.Assert.assertEquals("Forecast horizon is wrong: ", -1, specV3.getX11().getForecastHorizon());

    }

    @Test // Christiane Hofer
    public void testReadLegacy2() {
        X13Spec specInput = X13Spec.builder().build();

        InformationSet infoV2 = X13SpecMapping.write(specInput, null, true);
        InformationSet infoV2X11 = infoV2.getSubSet(X13SpecMapping.X11);
        infoV2X11.set(X11SpecMapping.FCASTS, -2);
        // The default value could be 0 vor X11 or -1 for X13 this is not saved in the SA Processiong of Version 2

        X13Spec specV3 = X13SpecMapping.readLegacy(infoV2, null);
        org.junit.Assert.assertEquals("Forecast horizon is wrong: ", -2, specV3.getX11().getForecastHorizon());

    }

    @Test
    public void testSaItem() {
        Ts ts = Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();

        SaDefinition sadef = SaDefinition.builder()
                .domainSpec(X13Spec.RSA5)
                .ts(ts)
                .build();

        SaItem item = SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(null, false);
        NameManager<SaSpecification> mgr = SaItemsMapping.defaultNameManager();
        InformationSet info = SaItemMapping.write(item, mgr, true, DemetraVersion.JD3);

        SaItem nitem = SaItemMapping.read(info, mgr, Collections.emptyMap());
        nitem.process(null, true);
    }

    @Test
    public void testSaItems() {
        Ts ts = Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();

        SaDefinition sadef = SaDefinition.builder()
                .domainSpec(X13Spec.RSA5)
                .ts(ts)
                .build();

        SaItem item = SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(null, false);

        SaItems items = SaItems.builder()
                .item(item)
                .build();

        InformationSet info = SaItemsMapping.write(items, true, DemetraVersion.JD3);

        SaItems nitems = SaItemsMapping.read(info);
        nitems.getItems().forEach(v -> v.process(null, true));
    }

    public static void testXmlDeserializationLegacy() {
        String tmp = Files.temporaryFolderPath();
        try {
            XmlInformationSet rslt = Jaxb.Parser
                    .of(XmlInformationSet.class)
                    .parseFile(Path.of(tmp + "saprocessing-2.xml").toFile());

            InformationSet info = rslt.create();
            SaItems nspec = SaItemsMapping.read(info);
            nspec.getItems().forEach(v -> v.process(null, false));
            System.out.println(nspec.getItems().size());
//            nspec.getItems().forEach(v -> System.out.println(((TramoSeatsResults) v.getEstimation().getResults()).getPreprocessing().getEstimation().getStatistics().getLogLikelihood()));
            long t0 = System.currentTimeMillis();
            nspec.getItems().forEach(v
                    -> {
                v.process(null, false);
                SaEstimation estimation = v.getEstimation();
                X13Results results = (X13Results) estimation.getResults();
                System.out.println(results.getPreprocessing().getEstimation().getStatistics().getLogLikelihood());
            }
            );
//            System.out.println(nspec.getItems().get(0).getDefinition().getDomainSpec().equals(TramoSeatsSpec.RSA5));
            long t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
        } catch (IOException ex) {
        }
    }

    public static void main(String[] arg) throws JAXBException, IOException {
        testXmlDeserializationLegacy();
    }
}
