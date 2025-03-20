/*
 * Copyright 2021 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.api.information;

import jdplus.toolkit.base.api.util.WildCards;
import nbbrd.design.Development;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic description of the results of a processing.
 * Except in trivial cases, all processing should generate results that
 * implements this interface
 * The returned objects should belong to the general API
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface Explorable {
    
    
    /**
     * Chck that this object is valid
     * @return 
     */
    default boolean isValid(){
        return true;
    }

    /**
     * Indicates that the provider can provide information on the mentioned item
     *
     * @param id Information item
     * @return
     */
    boolean contains(String id);

    /**
     * Gets the dictionary of all the possible results
     *
     * @param compact
     * @return
     */
    Map<String, Class> getDictionary();

    /**
     * Gets information related to the specified id
     * The identifier and the type should come from the dictionary provided by
     * this object
     *
     * @param <T>
     * @param id Name of information
     * @param tclass Class of the information
     * @return null if this information is not available
     */
    <T> T getData(String id, Class<T> tclass);

    default Object getData(String id) {
        return getData(id, Object.class);
    }

    /**
     * Gets all information corresponding to the given pattern and with the
     * right type
     *
     * @param <T>
     * @param pattern The pattern
     * @param tclass Type of information
     * @return
     */
    <T> Map<String, T> searchAll(String pattern, Class<T> tclass);

    public static final char SEP = '.';

    /**
     * Concatenates arrays of strings without separator
     *
     * @param s
     * @return
     */
    public static String paste(String... s) {
        switch (s.length) {
            case 0:
                return "";
            case 1:
                return s[0];
            default:
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length; ++i) {
                    builder.append(s[i]);
                }
                return builder.toString();
        }
    }

    /**
     * Concatenates arrays of strings with the default separator ('.')
     *
     * @param s
     * @return
     */
    public static String spaste(String... s) {
        switch (s.length) {
            case 0:
                return "";
            case 1:
                return s[0];
            default:
                boolean first = true;
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length; ++i) {
                    if (s[i] != null) {
                        if (!first) {
                            builder.append(SEP);
                        } else {
                            first = false;
                        }
                        builder.append(s[i]);
                    }
                }
                return builder.toString();
        }
    }

    public static <X> Explorable of(X source, String prefix, BasicInformationExtractor<X> extractor) {
        return new Explorable() {
            /**
             * Indicates that the provider can provide information on the
             * mentioned item
             *
             * @param id Information item
             * @return
             */
            @Override
            public boolean contains(String id) {
                return extractor.contains(id);
            }

            /**
             * Gets the dictionary of all the possible results
             *
             * @return
             */
            @Override
            public Map<String, Class> getDictionary() {
                LinkedHashMap<String, Class> map = new LinkedHashMap<>();
                extractor.fillDictionary(prefix, map, true);
                return map;
            }

            /**
             * Gets information related to the specified id
             * The identifier and the type should come from the dictionary
             * provided by
             * this object
             *
             * @param <T>
             * @param id Name of information
             * @param tclass Class of the information
             * @return null if this information is not available
             */
            @Override
            public <T> T getData(String id, Class<T> tclass) {
                return extractor.getData(source, id, tclass);
            }

            @Override
            public <T> Map<String, T> searchAll(String pattern, Class<T> tclass) {
                Map<String, T> rslt = new LinkedHashMap<>();
                extractor.searchAll(source, new WildCards(pattern), tclass, rslt);
                return rslt;
            }
        };
    }

}
