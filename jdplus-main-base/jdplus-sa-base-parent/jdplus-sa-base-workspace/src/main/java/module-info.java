module jdplus.sa.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.workspace;
    requires jdplus.sa.base.information;

    exports demetra.sa.workspace;

    provides demetra.workspace.file.spi.FamilyHandler with
            demetra.sa.workspace.SaHandlers.SaMulti,
            demetra.sa.workspace.SaHandlers.SaMultiLegacy;
}