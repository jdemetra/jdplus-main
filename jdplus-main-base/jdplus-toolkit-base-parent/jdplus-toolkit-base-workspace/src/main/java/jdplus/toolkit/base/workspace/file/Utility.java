/*
 * Copyright 2024 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.workspace.file;

import internal.toolkit.base.workspace.file.xml.util.XmlCalendars;
import internal.toolkit.base.workspace.file.xml.util.XmlTsVariables;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import jdplus.toolkit.base.api.timeseries.calendars.CalendarManager;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import nbbrd.io.xml.Xml;
import nbbrd.io.xml.bind.Jaxb;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Utility {

    private static final Xml.Parser<CalendarManager> CAL_R = Jaxb.Parser.of(XmlCalendars.class).andThen(x -> x.create());
    private static final Xml.Parser<TsDataSuppliers> VARS_R = Jaxb.Parser.of(XmlTsVariables.class).andThen(x -> x.create());
    private static final Xml.Formatter<CalendarManager> CAL_W = Jaxb.Formatter.of(XmlCalendars.class)
            .withFormatted(true)
            .compose(v -> {
                XmlCalendars x = new XmlCalendars();
                x.copy(v);
                return x;
            });
    private static final Xml.Formatter<TsDataSuppliers> VARS_W = Jaxb.Formatter.of(XmlTsVariables.class)
            .withFormatted(true)
            .compose(v -> {
                XmlTsVariables x = new XmlTsVariables();
                x.copy(v);
                return x;
            });

    public CalendarManager readCalendars(String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        return CAL_R.parseFile(file);
    }

    public TsDataSuppliers readData(String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        return VARS_R.parseFile(file);
    }

    public void writeCalendars(CalendarManager cal, String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        CAL_W.formatFile(cal, file);
    }

    public void writeData(TsDataSuppliers data, String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        VARS_W.formatFile(data, file);
    }

}
