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

import java.util.stream.Stream;

/**
 *
 * @author PALATEJ
 */
@lombok.Getter
@lombok.AllArgsConstructor
public class PrefixedDictionary implements Dictionary {

    String path;

    Dictionary core;
    
    EntryType type; 
    
    public PrefixedDictionary(String path, Dictionary core){
        this(path, core, EntryType.Normal);
    }

    @Override
    public Stream<? extends Entry> entries() {
        return path == null || path.length() == 0 ? core.entries()
                : core.entries().map(entry -> new DerivedItem(entry));
    }
    
    @Override
    public Stream<String> keys() {
        return path == null || path.length() == 0 ? core.keys()
                : core.keys().map(keyitem -> derivedName(Dictionary.fullName(path, type), keyitem));
    }

    @Override
    public Stream<String> keys(Class tclass) {
        return path == null || path.length() == 0 ? core.keys(tclass)
                : core.keys()
                        .filter(item -> tclass.isAssignableFrom(item.getClass()))
                        .map(keyitem -> derivedName(Dictionary.fullName(path, type), keyitem));
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public class DerivedItem implements Entry {

        Entry core;

        @Override
        public String getName() {
            return derivedName(Dictionary.fullName(path, type), core.getName());
        }

        @Override
        public String getDescription() {
            return core.getDescription();
        }

        @Override
        public String getDetail() {
            return core.getDetail();
        }

        @Override
        public Class getOutputClass() {
            return core.getOutputClass();
        }

        @Override
        public EntryType getType() {
            return core.getType();
        }

    }

    private static String derivedName(String path, String name) {
        return new StringBuilder()
                .append(path)
                .append(SEP)
                .append(name)
                .toString();

    }

}
