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
package internal.spreadsheet.base.api;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.BookFactoryLoader;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
//@MightBePromoted
@lombok.Builder
public final class SpreadsheetManager {

    @StaticFactoryMethod
    public static @NonNull SpreadsheetManager ofServiceLoader() {
        return SpreadsheetManager.builder().factories(BookFactoryLoader.get()).build();
    }

    @lombok.Singular
    private final List<Book.Factory> factories;

    public @NonNull Optional<Book.Factory> getReader(@NonNull File file) {
        return factories
                .stream()
                .filter(Book.Factory::canLoad)
                .filter(factory -> factory.accept(file))
                .findFirst();
    }

    public @NonNull Optional<Book.Factory> getWriter(@NonNull File file) {
        return factories
                .stream()
                .filter(Book.Factory::canStore)
                .filter(factory -> factory.accept(file))
                .findFirst();
    }
}
