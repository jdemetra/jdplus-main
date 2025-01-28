/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.xml.legacy.regression;

import jdplus.toolkit.base.api.data.ParameterType;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.xml.legacy.core.XmlParameter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Validator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import xml.Schemas;
import xml.TestValidationEventHandler;

/**
 *
 * @author Jean Palate
 */
public class XmlRegressionTest {

    private static final String FILE = "c:\\localdata\\regression.xml";

    private XmlRegression reg;

    public XmlRegressionTest() {
        // create the regression
        XmlAdditiveOutlier xao = new XmlAdditiveOutlier();
        xao.setPosition(LocalDate.now(Clock.systemDefaultZone()));
        XmlSeasonalOutlier xso = new XmlSeasonalOutlier();
        xso.setPosition(LocalDate.now(Clock.systemDefaultZone()).minusDays(5000));

        reg = new XmlRegression();
        XmlRegressionItem ao = new XmlRegressionItem();
        ao.variable = xao;
        ao.coefficient= new XmlParameter(1000, ParameterType.Fixed);
        reg.getItems().add(ao);
        XmlRegressionItem so = new XmlRegressionItem();
        so.variable = xso;
        reg.getItems().add(so);

        XmlGenericTradingDays xtd = new XmlGenericTradingDays();
        xtd.contrasts = true;
        xtd.dayClustering = DayClustering.TD4.getGroupsDefinition();
        XmlVariableWindow xwnd = new XmlVariableWindow();
        xwnd.From = LocalDate.of(2000, 1, 1);
        xwnd.To = LocalDate.of(2010, 12, 31);
        xtd.getModifiers().add(xwnd);

        XmlRegressionItem td = new XmlRegressionItem();
        td.variable = xtd;
        reg.getItems().add(td);
    }

    @Test
    @Disabled
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException {

        JAXBContext jaxb = XmlRegression.context();
        try (Writer writer = Files.newBufferedWriter(Path.of(FILE), StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(reg, writer);
            writer.flush();
        }

        XmlRegression rslt = null;
        try (Reader reader = Files.newBufferedReader(Path.of(FILE), StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            unmarshaller.setSchema(Schemas.Modelling);
            unmarshaller.setEventHandler(new TestValidationEventHandler());
            rslt = (XmlRegression) unmarshaller.unmarshal(reader);
            for (XmlRegressionItem item : rslt.getItems()){
                ITsVariable tsvar = item.variable.toTsVariable();
                assertTrue(tsvar != null);
            }
        }
    }

    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        JAXBContext jaxb = XmlRegression.context();
        JAXBSource source = new JAXBSource(jaxb, reg);
        Validator validator = Schemas.Modelling.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }
}
