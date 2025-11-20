module jdplus.toolkit.base.xml {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.toolkit.base.api;
    requires nbbrd.io.xml.bind;

    exports jdplus.toolkit.base.xml.information;
    exports jdplus.toolkit.base.xml.legacy;
    exports jdplus.toolkit.base.xml.legacy.arima;
    exports jdplus.toolkit.base.xml.legacy.calendars;
    exports jdplus.toolkit.base.xml.legacy.core;
    exports jdplus.toolkit.base.xml.legacy.modelling;
    exports jdplus.toolkit.base.xml.legacy.processing;
    exports jdplus.toolkit.base.xml.legacy.regression;

    uses jdplus.toolkit.base.xml.legacy.regression.TsModifierAdapter;
    uses jdplus.toolkit.base.xml.legacy.calendars.CalendarAdapter;
    uses jdplus.toolkit.base.xml.legacy.regression.TsVariableAdapter;
    uses jdplus.toolkit.base.xml.legacy.calendars.HolidayAdapter;

    provides jdplus.toolkit.base.xml.legacy.regression.TsVariableAdapter with
            jdplus.toolkit.base.xml.legacy.regression.XmlAdditiveOutlier.Adapter,
            jdplus.toolkit.base.xml.legacy.regression.XmlGenericTradingDays.Adapter,
            jdplus.toolkit.base.xml.legacy.regression.XmlInterventionVariable.Adapter,
            jdplus.toolkit.base.xml.legacy.regression.XmlLevelShift.Adapter,
            jdplus.toolkit.base.xml.legacy.regression.XmlRamp.Adapter,
            jdplus.toolkit.base.xml.legacy.regression.XmlSeasonalOutlier.Adapter,
            jdplus.toolkit.base.xml.legacy.regression.XmlTransitoryChange.Adapter;

    provides jdplus.toolkit.base.xml.legacy.calendars.HolidayAdapter with
            jdplus.toolkit.base.xml.legacy.calendars.XmlEasterRelatedDay.Adapter,
            jdplus.toolkit.base.xml.legacy.calendars.XmlFixedDay.Adapter,
            jdplus.toolkit.base.xml.legacy.calendars.XmlSpecialCalendarDay.Adapter;

    provides jdplus.toolkit.base.xml.legacy.regression.TsModifierAdapter with
            jdplus.toolkit.base.xml.legacy.regression.XmlLags.Adapter,
            jdplus.toolkit.base.xml.legacy.regression.XmlVariableWindow.Adapter;

    provides jdplus.toolkit.base.xml.legacy.calendars.CalendarAdapter with
            jdplus.toolkit.base.xml.legacy.calendars.XmlChainedCalendar.Adapter,
            jdplus.toolkit.base.xml.legacy.calendars.XmlCompositeCalendar.Adapter,
            jdplus.toolkit.base.xml.legacy.calendars.XmlNationalCalendar.Adapter;
}