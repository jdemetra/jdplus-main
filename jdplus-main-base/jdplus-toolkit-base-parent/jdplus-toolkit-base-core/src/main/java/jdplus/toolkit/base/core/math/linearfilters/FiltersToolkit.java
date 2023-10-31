/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.toolkit.base.core.math.linearfilters;

import java.util.HashMap;
import jdplus.toolkit.base.api.information.InformationMapping;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.math.linearfilters.FilterSpec;
import jdplus.toolkit.base.api.math.linearfilters.HendersonSpec;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.toolkit.base.api.math.linearfilters.UserDefinedFilterSpec;
import jdplus.toolkit.base.api.math.linearfilters.UserDefinedSymmetricFilterSpec;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class FiltersToolkit {

    private final Map< Class, Function<FilterSpec, IFiltering>> map = new HashMap<>();

    static {
        map.put(HendersonSpec.class, spec -> HendersonFilters.of((HendersonSpec) spec));
        map.put(LocalPolynomialFilterSpec.class, spec -> LocalPolynomialFilters.of((LocalPolynomialFilterSpec) spec));
        map.put(UserDefinedSymmetricFilterSpec.class, spec ->{
            if (spec instanceof UserDefinedSymmetricFilterSpec uspec){
                // NOT CHECKED ! We suppose that the central filter is symmetric
                return SymmetricFiltering.of(uspec.getCentralFilter(), uspec.getEndPointsFilters());
            }else
                return null;
        } );
        map.put(UserDefinedFilterSpec.class, spec ->{
            if (spec instanceof UserDefinedFilterSpec uspec){
                return Filtering.of(uspec.getCentralFilter(), uspec.getLFilters(), uspec.getUFilters());
            }else
                return null;
        } );
    }

    public <S extends FilterSpec> void register(Class<S> spec, Function<S, IFiltering> fn) {
        synchronized (map) {
            map.put(spec, (Function<FilterSpec, IFiltering>) fn);
        }
    }
    
    public <S extends FilterSpec> void unregister(Class<S> spec) {
        synchronized (map) {
            map.remove(spec);
        }
    }

    public IFiltering of(FilterSpec spec){
        synchronized(map){
            Function<FilterSpec, IFiltering> fn=map.get(spec.getClass());
            if (fn == null)
                throw new LinearFilterException("Filter spec not registered");
            return fn.apply(spec);
        }
    }

    @lombok.Value
    @lombok.AllArgsConstructor
    @lombok.Builder
    @Deprecated
    public static class FiniteFilters implements GenericExplorable {

        private SymmetricFilter filter;
        private IFiniteFilter[] afilters;

        private static final InformationMapping<FiniteFilters> MAPPING = new InformationMapping<FiniteFilters>() {
            @Override
            public Class getSourceClass() {
                return FiniteFilters.class;
            }
        };

        public static final InformationMapping<FiniteFilters> getMapping() {
            return MAPPING;
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        static {
            MAPPING.set("svariancereduction", Double.class, source -> varianceReduction(source.filter));
            MAPPING.setArray("avariancereduction", 0, Double.class, (source, i)
                    -> i < source.afilters[i].length() ? varianceReduction(source.afilters[i]) : Double.NaN);
            MAPPING.set("sbias2", Double.class, source -> bias2(source.filter));
            MAPPING.setArray("abias0", 0, Double.class, (source, i)
                    -> i < source.afilters[i].length() ? bias0(source.afilters[i]) : Double.NaN);
            MAPPING.setArray("abias1", 0, Double.class, (source, i)
                    -> i < source.afilters[i].length() ? bias1(source.afilters[i]) : Double.NaN);
            MAPPING.setArray("abias2", 0, Double.class, (source, i)
                    -> i < source.afilters[i].length() ? bias2(source.afilters[i]) : Double.NaN);
            MAPPING.set("sweights", double[].class, source -> source.filter.weightsToArray());
            MAPPING.setArray("aweights", 0, double[].class, (source, i) -> i < source.afilters[i].length() ? source.afilters[i].weightsToArray() : null);
            MAPPING.set("sgain", double[].class, source -> gain(source.filter));
            MAPPING.setArray("again", 0, double[].class, (source, i) -> i < source.afilters[i].length() ? gain(source.afilters[i]) : null);
            MAPPING.setArray("aphase", 0, double[].class, (source, i) -> i < source.afilters[i].length() ? phase(source.afilters[i]) : null);
        }
    }

    public double[] gain(IFilter filter) {
        DoubleUnaryOperator gainFunction = filter.gainFunction();
        int RES = 600;
        double[] g = new double[RES + 1];
        for (int i = 0; i <= RES; ++i) {
            g[i] = gainFunction.applyAsDouble(i * Math.PI / 600);
        }
        return g;
    }

    public double varianceReduction(IFiniteFilter filter) {
        double s = 0;
        int l = filter.getLowerBound(), u = filter.getUpperBound();
        IntToDoubleFunction weights = filter.weights();
        for (int i = l; i <= u; ++i) {
            double w = weights.applyAsDouble(i);
            s += w * w;
        }
        return s;
    }

    public double bias0(IFiniteFilter filter) {
        double s = 0;
        int l = filter.getLowerBound(), u = filter.getUpperBound();
        IntToDoubleFunction weights = filter.weights();
        for (int i = l; i <= u; ++i) {
            double w = weights.applyAsDouble(i);
            s += w;
        }
        return s;
    }

    public double bias1(IFiniteFilter filter) {
        double s = 0;
        int l = filter.getLowerBound(), u = filter.getUpperBound();
        IntToDoubleFunction weights = filter.weights();
        for (int i = l; i <= u; ++i) {
            double w = i * weights.applyAsDouble(i);
            s += w;
        }
        return s;
    }

    public double bias2(IFiniteFilter filter) {
        double s = 0;
        int l = filter.getLowerBound(), u = filter.getUpperBound();
        IntToDoubleFunction weights = filter.weights();
        for (int i = l; i <= u; ++i) {
            double w = i * i * weights.applyAsDouble(i);
            s += w;
        }
        return s;
    }

    public double[] phase(IFilter filter) {
        DoubleUnaryOperator phaseFunction = filter.phaseFunction();
        int RES = 600;
        double[] g = new double[RES + 1];
        for (int i = 0; i <= RES; ++i) {
            g[i] = phaseFunction.applyAsDouble(i * Math.PI / 600);
        }
        return g;
    }
}
