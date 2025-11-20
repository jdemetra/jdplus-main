import internal.toolkit.base.workspace.file.GenericHandlers;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;

module jdplus.toolkit.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.xml;
    requires nbbrd.io.xml.bind;
    requires java.logging;

    exports jdplus.toolkit.base.workspace;
    exports jdplus.toolkit.base.workspace.file;
    exports jdplus.toolkit.base.workspace.file.spi;
    exports jdplus.toolkit.base.workspace.file.util;

    uses FamilyHandler;

    provides FamilyHandler with
            GenericHandlers.UtilCal,
            GenericHandlers.UtilVar;
}