/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.x13.base.workspace;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.information.InformationSetSerializer;
import jdplus.toolkit.base.information.TsDocumentMapping;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.informationSet;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.parse;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import jdplus.x13.base.information.RegArimaSpecMapping;
import jdplus.x13.base.information.X13SpecMapping;
import jdplus.x13.base.core.x13.X13Document;
import jdplus.x13.base.core.x13.regarima.RegArimaDocument;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class X13Handlers {

    public final WorkspaceFamily SA_DOC_X13 = parse("Seasonal adjustment@documents@x13");
    public final WorkspaceFamily SA_SPEC_X13 = parse("Seasonal adjustment@specifications@x13");

    public final WorkspaceFamily MOD_DOC_REGARIMA = parse("Modelling@documents@regarima");
    public final WorkspaceFamily MOD_SPEC_REGARIMA = parse("Modelling@specifications@regarima");

    public static final String REGARIMADOC_PREFIX="RegArimaDoc";
    public static final String REGARIMADOC_REPO="RegArimaDoc";
    public static final String X13DOC_PREFIX="X13Doc";
    public static final String X13DOC_REPO="X13Doc";
    public static final String REGARIMASPEC_PREFIX="RegArimaSpec";
    public static final String REGARIMASPEC_REPO="RegArimaSpec";
    public static final String X13SPEC_PREFIX="X13Spec";
    public static final String X13SPEC_REPO="X13Spec";

    @ServiceProvider(FamilyHandler.class)
    public static final class SaDocX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_X13,
                new InformationSetSerializer<X13Document>() {
            @Override
            public InformationSet write(X13Document object, boolean verbose) {
                return TsDocumentMapping.write(object, X13SpecMapping.SERIALIZER_V3, verbose, true);
            }

            @Override
            public X13Document read(InformationSet info) {

                X13Document doc = new X13Document();
                TsDocumentMapping.read(info, X13SpecMapping.SERIALIZER_V3, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }
        }, X13DOC_REPO);

    }

    public static final class SaDocX13Legzcy implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_X13,
                new InformationSetSerializer<X13Document>() {
            @Override
            public InformationSet write(X13Document object, boolean verbose) {
                return TsDocumentMapping.write(object, X13SpecMapping.SERIALIZER_LEGACY, verbose, true);
            }

            @Override
            public X13Document read(InformationSet info) {

                X13Document doc = new X13Document();
                TsDocumentMapping.read(info, X13SpecMapping.SERIALIZER_LEGACY, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }
        }, X13DOC_REPO);

    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_SPEC_X13, X13SpecMapping.SERIALIZER_V3, X13SPEC_REPO);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecX13Legacy implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_SPEC_X13, X13SpecMapping.SERIALIZER_LEGACY, X13SPEC_REPO);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModDocRegarima implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_REGARIMA,
                new InformationSetSerializer<RegArimaDocument>() {
            @Override
            public InformationSet write(RegArimaDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, RegArimaSpecMapping.SERIALIZER_V3, verbose, true);
            }

            @Override
            public RegArimaDocument read(InformationSet info) {

                RegArimaDocument doc = new RegArimaDocument();
                TsDocumentMapping.read(info, RegArimaSpecMapping.SERIALIZER_V3, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }
        }, REGARIMADOC_REPO);

    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModDocRegarimaLegacy implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_REGARIMA,
                new InformationSetSerializer<RegArimaDocument>() {
            @Override
            public InformationSet write(RegArimaDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, RegArimaSpecMapping.SERIALIZER_LEGACY, verbose, true);
            }

            @Override
            public RegArimaDocument read(InformationSet info) {

                RegArimaDocument doc = new RegArimaDocument();
                TsDocumentMapping.read(info, RegArimaSpecMapping.SERIALIZER_LEGACY, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }
        }, REGARIMADOC_REPO);

    }

    @ServiceProvider(FamilyHandler.class)

    public static final class ModSpecRegarima implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_SPEC_REGARIMA, RegArimaSpecMapping.SERIALIZER_V3, REGARIMASPEC_REPO);
    }

    public static final class ModSpecRegarimaLegacy implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_SPEC_REGARIMA, RegArimaSpecMapping.SERIALIZER_LEGACY, REGARIMASPEC_REPO);
    }
}
