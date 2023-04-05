/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package jdplus.toolkit.desktop.plugin.ui.calendar.actions;

import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.Converter;
import jdplus.toolkit.base.api.timeseries.calendars.Calendar;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarManager;
import jdplus.toolkit.base.api.timeseries.calendars.ChainedCalendar;
import jdplus.toolkit.base.api.timeseries.calendars.CompositeCalendar;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import internal.toolkit.base.workspace.file.xml.util.AbstractXmlCalendar;
import internal.toolkit.base.workspace.file.xml.util.XmlChainedCalendar;
import internal.toolkit.base.workspace.file.xml.util.XmlCompositeCalendar;
import internal.toolkit.base.workspace.file.xml.util.XmlNationalCalendar;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.xml.bind.Jaxb;

/**
 *
 * @author Philippe Charles
 */
final class CalendarConfig implements Converter<CalendarDefinition, Config> {

    public static final String DOMAIN = AbstractXmlCalendar.class.getName();

    private final Formatter<XmlNationalCalendar> nationalFormatter = Jaxb.Formatter.of(XmlNationalCalendar.class).asFormatter();
    private final Formatter<XmlChainedCalendar> chainedFormatter = Jaxb.Formatter.of(XmlChainedCalendar.class).asFormatter();
    private final Formatter<XmlCompositeCalendar> compositeFormatter = Jaxb.Formatter.of(XmlCompositeCalendar.class).asFormatter();

    private final Parser<XmlNationalCalendar> nationalParser = Jaxb.Parser.of(XmlNationalCalendar.class).asParser();
    private final Parser<XmlChainedCalendar> chainedParser = Jaxb.Parser.of(XmlChainedCalendar.class).asParser();
    private final Parser<XmlCompositeCalendar> compositeParser = Jaxb.Parser.of(XmlCompositeCalendar.class).asParser();

    @Override
    public Config doForward(CalendarDefinition cal) {
        CalendarManager manager = ModellingContext.getActiveContext().getCalendars();
        Config.Builder result = Config.builder(DOMAIN, manager.get(cal), "3.0");
        String code = manager.get(cal);
        result.parameter("type", cal.getClass().getName());
        String xml = format(cal, code, manager);
        if (xml == null) {
            throw new RuntimeException("Cannot format calendar");
        }
        result.parameter("xml", xml);
        return result.build();
    }

    private String format(CalendarDefinition cal, String code, CalendarManager manager) {
        if (cal instanceof Calendar) {
            return nationalFormatter.formatAsString(XmlNationalCalendar.create(code, manager));
        } else if (cal instanceof ChainedCalendar) {
            return chainedFormatter.formatAsString(XmlChainedCalendar.create(code, manager));
        } else if (cal instanceof CompositeCalendar) {
            return compositeFormatter.formatAsString(XmlCompositeCalendar.create(code, manager));
        }
        return null;
    }

    @Override
    public CalendarDefinition doBackward(Config config) {
        CalendarManager manager = ModellingContext.getActiveContext().getCalendars();
        AbstractXmlCalendar xmlCal = parse(config.getParameter("type"), config.getParameter("xml"));
        if (xmlCal != null) {
            if (xmlCal.addTo(manager)) {
                return manager.get(xmlCal.name);
            }
            throw new IllegalArgumentException("Cannot add calendar to manager");
        }
        throw new IllegalArgumentException("Cannot parse config");
    }

    private AbstractXmlCalendar parse(String type, String xml) {
        if (Calendar.class.getName().equals(type)) {
            return nationalParser.parse(xml);
        } else if (ChainedCalendar.class.getName().equals(type)) {
            return chainedParser.parse(xml);
        } else if (CompositeCalendar.class.getName().equals(type)) {
            return compositeParser.parse(xml);
        } else {
            return null;
        }
    }
}
