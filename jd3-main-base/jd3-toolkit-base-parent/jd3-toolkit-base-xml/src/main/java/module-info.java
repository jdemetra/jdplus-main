@SuppressWarnings("JavaModuleNaming")
module jd3.toolkit.base.xml {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jd3.toolkit.base.api;
    requires nbbrd.io.xml.bind;

    exports demetra.toolkit.io.xml.information;
    exports demetra.toolkit.io.xml.legacy;
    exports demetra.toolkit.io.xml.legacy.arima;
    exports demetra.toolkit.io.xml.legacy.calendars;
    exports demetra.toolkit.io.xml.legacy.core;
    exports demetra.toolkit.io.xml.legacy.modelling;
    exports demetra.toolkit.io.xml.legacy.processing;
    exports demetra.toolkit.io.xml.legacy.regression;

    uses demetra.toolkit.io.xml.legacy.regression.TsModifierAdapter;
    uses demetra.toolkit.io.xml.legacy.calendars.CalendarAdapter;
    uses demetra.toolkit.io.xml.legacy.regression.TsVariableAdapter;
    uses demetra.toolkit.io.xml.legacy.calendars.HolidayAdapter;

    provides demetra.toolkit.io.xml.legacy.regression.TsVariableAdapter with
            demetra.toolkit.io.xml.legacy.regression.XmlAdditiveOutlier.Adapter,
            demetra.toolkit.io.xml.legacy.regression.XmlGenericTradingDays.Adapter,
            demetra.toolkit.io.xml.legacy.regression.XmlInterventionVariable.Adapter,
            demetra.toolkit.io.xml.legacy.regression.XmlLevelShift.Adapter,
            demetra.toolkit.io.xml.legacy.regression.XmlRamp.Adapter,
            demetra.toolkit.io.xml.legacy.regression.XmlSeasonalOutlier.Adapter,
            demetra.toolkit.io.xml.legacy.regression.XmlTransitoryChange.Adapter;

    provides demetra.toolkit.io.xml.legacy.calendars.HolidayAdapter with
            demetra.toolkit.io.xml.legacy.calendars.XmlEasterRelatedDay.Adapter,
            demetra.toolkit.io.xml.legacy.calendars.XmlFixedDay.Adapter,
            demetra.toolkit.io.xml.legacy.calendars.XmlSpecialCalendarDay.Adapter;

    provides demetra.toolkit.io.xml.legacy.regression.TsModifierAdapter with
            demetra.toolkit.io.xml.legacy.regression.XmlLags.Adapter,
            demetra.toolkit.io.xml.legacy.regression.XmlVariableWindow.Adapter;

    provides demetra.toolkit.io.xml.legacy.calendars.CalendarAdapter with
            demetra.toolkit.io.xml.legacy.calendars.XmlChainedCalendar.Adapter,
            demetra.toolkit.io.xml.legacy.calendars.XmlCompositeCalendar.Adapter,
            demetra.toolkit.io.xml.legacy.calendars.XmlNationalCalendar.Adapter;
}