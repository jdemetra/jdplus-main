import jdplus.sa.base.workspace.SaHandlers;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;

module jdplus.sa.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.workspace;
    requires jdplus.sa.base.information;

    exports jdplus.sa.base.workspace;

    provides FamilyHandler with
            SaHandlers.SaMulti,
            SaHandlers.SaMultiLegacy;
}