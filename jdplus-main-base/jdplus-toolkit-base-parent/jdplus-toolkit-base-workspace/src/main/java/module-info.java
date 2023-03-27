module jdplus.toolkit.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.xml;
    requires nbbrd.io.xml.bind;
    requires java.logging;

    exports demetra.workspace;
    exports demetra.workspace.file.spi;
    exports demetra.workspace.file.util;

    uses demetra.workspace.file.spi.FamilyHandler;

    provides demetra.workspace.file.spi.FamilyHandler with
            internal.workspace.file.GenericHandlers.UtilCal,
            internal.workspace.file.GenericHandlers.UtilVar;
}