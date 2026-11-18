/*
 * Copyright 2026 JDemetra+.
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
package jdplus.toolkit.base.api.timeseries.regression;

import java.util.HashMap;
import java.util.Map;
import jdplus.toolkit.base.api.data.Parameter;
import nbbrd.design.Development;

/**
 * This variable should be translated to the correct implementation when the
 * context is known
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class UserVariableSpec {

    public static UserVariableSpec EMPTY = builder().id("").build();

    @lombok.With
    private String name;

    @lombok.With
    private String description;

    @lombok.With
    @lombok.NonNull private String id;

    @lombok.With
    @lombok.Singular
    @lombok.NonNull private Map<String, String> attributes;

    @lombok.With
    int lag;

    @lombok.With
    private Parameter coefficient;

    public boolean isLag() {
        return lag != 0;
    }

    public String getName() {
        return name == null ? toString() : name;
    }

    public String getDescription() {
        return description == null ? toString() : description;
    }

    public Parameter getCoefficient() {
        return coefficient == null ? Parameter.undefined() : coefficient;
    }

    public UserVariableSpec setAttribute(String key, String value) {
        Map<String, String> natts;
        natts = new HashMap<>(attributes);
        natts.put(key, value);
        return toBuilder().clearAttributes().attributes(natts).build();
    }

    public UserVariableSpec removeAttribute(String key) {
        if (!attributes.containsKey(key)) {
            return this;
        }
        Map<String, String> natts;
        natts = new HashMap<>(attributes);
        natts.remove(key);
        return toBuilder().clearAttributes().attributes(natts).build();
    }

    public Variable<UserVariable> instantiateFrom(ModellingContext context) {
        UserVariable var = UserVariable.of(id, getDescription(), context, lag);
        if (var == null) {
            return null;
        }
        return Variable.<UserVariable>builder()
                .core(var)
                .attributes(attributes)
                .name(getName())
                .coefficients(new Parameter[] {getCoefficient()})
                .build();
    }

    public static UserVariableSpec of(Variable<ITsVariable> var) {
        ITsVariable v = var.getCore();
        if (v instanceof UserVariable user) {
            return UserVariableSpec.builder()
                    .coefficient(var.getCoefficient(0))
                    .attributes(var.getAttributes())
                    .id((user).getId())
                    .description(v.description(null))
                    .name(var.getName())
                    .build();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        if (lag == 0) {
            return id;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(id).append('[');
        if (lag < 0) {
            builder.append('+');
        }
        builder.append(-lag);
        return builder.append(']').toString();
    }

    /**
     *
     * @return True if all coefficients are free, false otherwise
     */
    public boolean isFree() {
        return coefficient == null || coefficient.isFree();
    }

    public String attribute(String regeffect) {
        return attributes.get(regeffect);
    }
}
