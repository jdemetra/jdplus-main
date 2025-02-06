/*
 * Copyright 2021 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tramoseats.base.workspace;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.information.InformationSetSerializer;
import jdplus.toolkit.base.information.TsDocumentMapping;
import jdplus.tramoseats.base.information.TramoSeatsSpecMapping;
import jdplus.tramoseats.base.information.TramoSpecMapping;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.informationSet;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.parse;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import jdplus.tramoseats.base.core.tramo.TramoDocument;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDocument;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TramoSeatsHandlers {

    public final WorkspaceFamily SA_DOC_TRAMOSEATS = parse("Seasonal adjustment@documents@tramoseats");
    public final WorkspaceFamily SA_SPEC_TRAMOSEATS = parse("Seasonal adjustment@specifications@tramoseats");

    public final WorkspaceFamily MOD_DOC_TRAMO = parse("Modelling@documents@tramo");
    public final WorkspaceFamily MOD_SPEC_TRAMO = parse("Modelling@specifications@tramo");
    
    public static final String TRAMODOC_PREFIX="TramoDoc";
    public static final String TRAMODOC_REPO="TramoDoc";
    public static final String TRAMOSEATSDOC_PREFIX="TramoSeatsDoc";
    public static final String TRAMOSEATSDOC_REPO="TramoSeatsDoc";
    public static final String TRAMOSPEC_PREFIX="TramoSpec";
    public static final String TRAMOSPEC_REPO="TramoSpec";
    public static final String TRAMOSEATSSPEC_PREFIX="TramoSeatsSpec";
    public static final String TRAMOSEATSSPEC_REPO="TramoSeatsSpec";

    @ServiceProvider(FamilyHandler.class)
    public static final class ModDocTramo implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_TRAMO,
                new InformationSetSerializer<TramoDocument>() {
            @Override
            public InformationSet write(TramoDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, TramoSpecMapping.SERIALIZER_V3, verbose, true);
            }

            @Override
            public TramoDocument read(InformationSet info) {

                TramoDocument doc = new TramoDocument();
                TsDocumentMapping.read(info, TramoSpecMapping.SERIALIZER_V3, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }

        }, TRAMODOC_REPO);

    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModDocTramoLegacy implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_TRAMO,
                new InformationSetSerializer<TramoDocument>() {
            @Override
            public InformationSet write(TramoDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, TramoSpecMapping.SERIALIZER_LEGACY, verbose, true);
            }

            @Override
            public TramoDocument read(InformationSet info) {

                TramoDocument doc = new TramoDocument();
                TsDocumentMapping.read(info, TramoSpecMapping.SERIALIZER_LEGACY, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }

        }, TRAMODOC_REPO);

    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaDocTramoSeats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_TRAMOSEATS,
                new InformationSetSerializer<TramoSeatsDocument>() {
            @Override
            public InformationSet write(TramoSeatsDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, TramoSeatsSpecMapping.SERIALIZER_V3, verbose, true);
            }

            @Override
            public TramoSeatsDocument read(InformationSet info) {

                TramoSeatsDocument doc = new TramoSeatsDocument();
                TsDocumentMapping.read(info, TramoSeatsSpecMapping.SERIALIZER_V3, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }
        }, TRAMOSEATSDOC_REPO);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaDocTramoSeatsLegacy implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_TRAMOSEATS,
                new InformationSetSerializer<TramoSeatsDocument>() {
            @Override
            public InformationSet write(TramoSeatsDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, TramoSeatsSpecMapping.SERIALIZER_LEGACY, verbose, true);
            }

            @Override
            public TramoSeatsDocument read(InformationSet info) {

                TramoSeatsDocument doc = new TramoSeatsDocument();
                TsDocumentMapping.read(info, TramoSeatsSpecMapping.SERIALIZER_LEGACY, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }
        }, TRAMOSEATSDOC_REPO);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecTramoseats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(TramoSeatsHandlers.SA_SPEC_TRAMOSEATS, TramoSeatsSpecMapping.SERIALIZER_V3, TRAMOSEATSSPEC_REPO);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecTramoseatsLegacy implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(TramoSeatsHandlers.SA_SPEC_TRAMOSEATS, TramoSeatsSpecMapping.SERIALIZER_LEGACY, TRAMOSEATSSPEC_REPO);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModSpecTramo implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(TramoSeatsHandlers.MOD_SPEC_TRAMO, TramoSpecMapping.SERIALIZER_V3, TRAMOSPEC_REPO);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModSpecTramoLegacy implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(TramoSeatsHandlers.MOD_SPEC_TRAMO, TramoSpecMapping.SERIALIZER_LEGACY, TRAMOSPEC_REPO);
    }
}
