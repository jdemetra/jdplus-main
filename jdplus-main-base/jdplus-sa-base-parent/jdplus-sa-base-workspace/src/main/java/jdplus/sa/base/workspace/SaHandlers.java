package jdplus.sa.base.workspace;

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
import jdplus.sa.base.information.SaItemsMapping;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.informationSet;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.parse;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class SaHandlers {

    public final WorkspaceFamily SA_MULTI = parse("Seasonal adjustment@multi-documents");
    public static final String REPOSITORY = "SAProcessing";
    public static final String PREFIX = "SAProcessing";

    @ServiceProvider(FamilyHandler.class)
    public static final class SaMulti implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_MULTI, SaItemsMapping.SERIALIZER_V3, REPOSITORY);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaMultiLegacy implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_MULTI, SaItemsMapping.SERIALIZER_LEGACY, REPOSITORY);
    }
}
