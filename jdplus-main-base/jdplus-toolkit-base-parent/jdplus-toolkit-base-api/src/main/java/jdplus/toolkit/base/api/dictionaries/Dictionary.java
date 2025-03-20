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
import lombok.NonNull;

/**
 *
 * @author PALATEJ
 */
public interface Dictionary {
    
    public enum EntryType{
        Normal,
        Parametric,
        Array
    }
    
     public static String fullName(String name, EntryType type){
        return switch (type) {
            case Parametric -> name+"(?)";
            case Array -> name+"(*)";
            default -> name;
        };
        }


    interface Entry {

        String getName();

        String getDescription();

        String getDetail();

        Class getOutputClass();
        
        EntryType getType();
        
        default String fullName(){
            return Dictionary.fullName(getName(), getType());
        }
        
        default String display(){
            return new StringBuilder()
                    .append(fullName())
                    .append('\t')
                    .append(getDescription())
                    .append('\t')
                    .append(getOutputClass().getCanonicalName())
                    .toString();
        }
    }

    Stream<? extends Entry> entries();

    default Stream<String> keys() {
        return entries()
                .map(item -> item.getName());
    }

    default Stream<String> keys(Class tclass) {
        return entries()
                .filter(item -> tclass.isAssignableFrom(item.getClass()))
                .map(item -> item.getName());
    }

    public static String concatenate(@NonNull String... st) {
        if (st.length == 1) {
            return st[0];
        }
        StringBuilder builder = new StringBuilder();
        builder.append(st[0]);
        for (int i = 1; i < st.length; ++i) {
            builder.append(SEP).append(st[i]);
        }
        return builder.toString();
    }

    public static final char SEP = '.';

}
