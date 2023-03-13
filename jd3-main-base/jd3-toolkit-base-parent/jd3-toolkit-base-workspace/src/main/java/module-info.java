@SuppressWarnings("JavaModuleNaming")
module jd3.toolkit.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jd3.toolkit.base.api;
    requires jd3.toolkit.base.xml;
    requires nbbrd.io.xml.bind;
    requires java.logging;

    uses demetra.workspace.file.spi.FamilyHandler;

    provides demetra.workspace.file.spi.FamilyHandler with
            internal.workspace.file.GenericHandlers.UtilCal,
            internal.workspace.file.GenericHandlers.UtilVar;
}