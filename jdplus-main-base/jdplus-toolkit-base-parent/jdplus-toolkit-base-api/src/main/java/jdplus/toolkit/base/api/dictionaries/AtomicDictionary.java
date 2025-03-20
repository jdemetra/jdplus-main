/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.api.dictionaries;

import nbbrd.design.LombokWorkaround;

import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class AtomicDictionary implements Dictionary {

    @lombok.Value
    @lombok.Builder
    public static class Item implements Dictionary.Entry {
        
        public static final String EMPTY="", TODO="TODO";

        String name;
        String description;
        String detail;
        Class outputClass;
        
        EntryType type;

        @LombokWorkaround
        public static Item.Builder builder() {
            return new Builder().description(TODO).detail(EMPTY).type(EntryType.Normal);
        }
    }

    public String name;

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    @lombok.Singular("item")
    List<Item> items;

    @Override
    public Stream<? extends Entry> entries() {
        return items.stream();
    }
}
